import {AfterViewInit, Component, OnInit} from '@angular/core';
import {TableData} from '../md/md-table/md-table.component';
import * as Chartist from 'chartist';
import * as $ from 'jquery';
import {HttpClient} from '@angular/common/http';
import {IStatistics} from 'app/shared/model/statistics.model';
import {DashboardService} from 'app/dashboard/dashboard.service';
import {IChartStatistics} from 'app/shared/model/chart-statistics.model';
import * as moment from 'moment';
import {IChartStatisticsRequest} from 'app/shared/model/chart-statistics-request.model';
import {TypeStatistics} from 'app/shared/model/enumerations/type-statistics.model';
import {NgbCalendar, NgbDate, NgbDateParserFormatter, NgbDatepickerConfig} from "@ng-bootstrap/ng-bootstrap";

import {parseZone} from "moment";

// declare const $: any;

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styles: [`
    .form-group.hidden {
      width: 0;
      margin: 0;
      border: none;
      padding: 0;
    }
    .custom-day {
      text-align: center;
      padding: 0.185rem 0.25rem;
      display: inline-block;
      height: 2rem;
      width: 2rem;
    }
    .custom-day.focused {
      background-color: #e6e6e6;
    }
    .custom-day.range, .custom-day:hover {
      background-color: rgb(2, 117, 216);
      color: white;
    }
    .custom-day.faded {
      background-color: rgba(2, 117, 216, 0.5);
    }
  `]
})
export class DashboardComponent implements OnInit, AfterViewInit {
  // constructor(private navbarTitleService: NavbarTitleService, private notificationService: NotificationService) { }
  dashboardService: DashboardService;
  statistics: IStatistics = {};
  chartStatistics: IChartStatistics [] = [];

  hoveredDate: NgbDate;
  fromDate: NgbDate;
  toDate: NgbDate;

  dataMonthlyChart = {
    labels: []
    ,
    series: []
  };
  dataAnnualChart = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    series: []
  };

  constructor(private http: HttpClient, dashboardService: DashboardService,private calendar: NgbCalendar, public formatter: NgbDateParserFormatter,private config: NgbDatepickerConfig ) {
    this.dashboardService = dashboardService;
    const current = new Date();
    this.minDate = {
      year: current.getFullYear(),
      month: current.getMonth() + 1,
      day: current.getDate()
    };
    //this.fromDate = calendar.getToday();
    // this.toDate = calendar.getNext(calendar.getToday(), 'd', 10);
  }
  minDate = undefined;
  public tableData: TableData;
  public daterange: any = {};
  filterRangeDate: any = {};

  public options: any = {
    locale: {format: 'YYYY-MM-DD'},
    alwaysShowCalendars: false,
    autoApply: true,
    opens: 'center',
    timePicker: true,
    timePicker24Hour: true,
    autoUpdateInput: true,
    startDate: new Date(),
    endDate: new Date(new Date().setMonth(new Date().getMonth() + 1)),
  };


  formatDate(date: NgbDate) {
    // NgbDates use 1 for Jan, Moement uses 0, must substract 1 month for proper date conversion
    var ngbObj =  JSON.parse(JSON.stringify(date));
    var newMoment = moment();

    if (ngbObj) {
      ngbObj.month--;
      newMoment.month(ngbObj.month);
      newMoment.day(ngbObj.day);
      newMoment.year(ngbObj.year);
    }
    return newMoment;
  }

  public selectedDate(value: any) {
    this.daterange.start = value.start._d;
    this.daterange.end = value.end._d;
    this.filterRangeDate = {
      start: new Date(this.daterange.start),
      end: new Date(this.daterange.end)
    };
  }
  onDateSelection(date: NgbDate) {
    if (!this.fromDate && !this.toDate) {
      this.fromDate = date;
    } else if (this.fromDate && !this.toDate && date.after(this.fromDate)) {
      this.toDate = date;
      const chartMonthlyStatisticsRequest: IChartStatisticsRequest = {
        date: this.formatDate(this.toDate),
        type: TypeStatistics.MONTHLY
      };

      const chartAnnualStatisticsRequest: IChartStatisticsRequest = {
        date: this.formatDate(this.toDate),
        type: TypeStatistics.ANNUAL
      };

      this.dataAnnualChart.series.pop();
      this.dataAnnualChart.series.pop();
      this.dataMonthlyChart.series.pop();
      this.dataMonthlyChart.series.pop();
      this.getMonthlyChartStatistics(chartMonthlyStatisticsRequest);
      this.getAnnualChartStatistics(chartAnnualStatisticsRequest);
      this.getStatistics();
    } else {
      this.toDate = null;
      this.fromDate = date;
    }
  }

  isHovered(date: NgbDate) {
    return this.fromDate && !this.toDate && this.hoveredDate && date.after(this.fromDate) && date.before(this.hoveredDate);
  }

  isInside(date: NgbDate) {
    return date.after(this.fromDate) && date.before(this.toDate);
  }

  isRange(date: NgbDate) {
    return date.equals(this.fromDate) || date.equals(this.toDate) || this.isInside(date) || this.isHovered(date);
  }

  validateInput(currentValue: NgbDate, input: string): NgbDate {
    const parsed = this.formatter.parse(input);
    return parsed && this.calendar.isValid(NgbDate.from(parsed)) ? NgbDate.from(parsed) : currentValue;
  }

  startAnimationForLineChart(chart: any) {
      let seq: any, delays: any, durations: any;
      seq = 0;
      delays = 80;
      durations = 500;
      chart.on('draw', function(data: any) {

        if (data.type === 'line' || data.type === 'area') {
          data.element.animate({
            d: {
              begin: 600,
              dur: 700,
              from: data.path.clone().scale(1, 0).translate(0, data.chartRect.height()).stringify(),
              to: data.path.clone().stringify(),
              easing: Chartist.Svg.Easing.easeOutQuint
            }
          });
        } else if (data.type === 'point') {
              seq++;
              data.element.animate({
                opacity: {
                  begin: seq * delays,
                  dur: durations,
                  from: 0,
                  to: 1,
                  easing: 'ease'
                }
              });
          }
      });

      seq = 0;
  }
  startAnimationForBarChart(chart: any) {
      let seq2: any, delays2: any, durations2: any;
      seq2 = 0;
      delays2 = 80;
      durations2 = 500;
      chart.on('draw', function(data: any) {
        if (data.type === 'bar') {
            seq2++;
            data.element.animate({
              opacity: {
                begin: seq2 * delays2,
                dur: durations2,
                from: 0,
                to: 1,
                easing: 'ease'
              }
            });
        }
      });

      seq2 = 0;
  }


   getStatistics()  {
    this.dashboardService.getStatistics().subscribe(
      res => {
        this.statistics = res;

      },
      res => {
        alert('An error has occurred when get statistics ');
      }
    );

  }
   getMonthlyChartStatistics(chartStatisticsRequest: IChartStatisticsRequest )  {
    this.dashboardService.getChartStatistics(chartStatisticsRequest).subscribe(
      res => {
        const totalInvoiceList = [];
        const paidInvoiceList = [];
        let max = 0;
        res.forEach(statMonth => {
            totalInvoiceList.push(statMonth.totalInvoice);
            paidInvoiceList.push(statMonth.totalPaid);
            if( this.dataMonthlyChart.labels.indexOf(statMonth.day) == -1){
              this.dataMonthlyChart.labels.push(statMonth.day);
            }
            if (max < statMonth.totalInvoice) {
              max = statMonth.totalInvoice;
            }
        });

      // this.dataMonthlyChart.labels =daysList;
       this.dataMonthlyChart.series.push(totalInvoiceList);
       this.dataMonthlyChart.series.push(paidInvoiceList);

        const optionsColouredBarsChart: any = {
          lineSmooth: Chartist.Interpolation.simple({
            divisor: 2,
            fillHoles: false
          }),
          axisY: {
            showGrid: true,
            offset: 40,
            onlyInteger: true
          },
          axisX: {
            showGrid: false,
          },
          low: 0,
          high: this.roundToNearestTenUp(max),
          showPoint: true,
          height: '300px'
        };

        const colouredBarsChart = new Chartist.Line('#colouredBarsChart', this.dataMonthlyChart, optionsColouredBarsChart);
        this.startAnimationForLineChart(colouredBarsChart);
      },
      res => {
        alert('An error has occurred when get statistics');
      }
    );

  }
   getAnnualChartStatistics(chartStatisticsRequest: IChartStatisticsRequest) {
     this.dashboardService.getChartStatistics(chartStatisticsRequest).subscribe(
       res => {
         const totalInvoiceList = [];
         const paidInvoiceList = [];
         let max = 0;
         res.forEach(statYear => {
           totalInvoiceList.push(statYear.totalInvoice);
           paidInvoiceList.push(statYear.totalPaid);
           if (max < statYear.totalInvoice) {
           max = statYear.totalInvoice;
           }
         });

         this.dataAnnualChart.series.push(totalInvoiceList);
         this.dataAnnualChart.series.push(paidInvoiceList);

         const optionsMultipleBarsChart = {
           seriesBarDistance: 10,
           axisX: {
             showGrid: false
           },
           axisY: {
             onlyInteger: true
           },
           height: '300px'
         };

         const responsiveOptionsMultipleBarsChart: any = [
           ['screen and (max-width: 640px)', {
             seriesBarDistance: 5,
             axisX: {
               labelInterpolationFnc: function (value: any) {
                 return value[0];
               }
             },
             high: this.roundToNearestTenUp(max)
           }]
         ];

         const multipleBarsChart = new Chartist.Bar('#multipleBarsChart', this.dataAnnualChart, optionsMultipleBarsChart,
           responsiveOptionsMultipleBarsChart);
         // start animation for the Emails Subscription Chart
         this.startAnimationForBarChart(multipleBarsChart);

       },
       res => {
       alert('An error has occurred when get statistics');
     }
   );



   }

  public ngOnInit() {
   const chartMonthlyStatisticsRequest: IChartStatisticsRequest = {
     date: moment(),
     type: TypeStatistics.MONTHLY
    };

    const chartAnnualStatisticsRequest: IChartStatisticsRequest = {
      date: moment(),
      type: TypeStatistics.ANNUAL
    };
    this.getMonthlyChartStatistics(chartMonthlyStatisticsRequest);
    this.getAnnualChartStatistics(chartAnnualStatisticsRequest);
    this.getStatistics();
   }

   ngAfterViewInit() {
       const breakCards = true;
       if (breakCards === true) {
           // We break the cards headers if there is too much stress on them :-)
           $('[data-header-animation="true"]').each(function() {
               const $fix_button = $(this);
               const $card = $(this).parent('.card');
               $card.find('.fix-broken-card').click(function() {
                   const $header = $(this).parent().parent().siblings('.card-header, .card-image');
                   $header.removeClass('hinge').addClass('fadeInDown');

                   $card.attr('data-count', 0);

                   setTimeout(function() {
                       $header.removeClass('fadeInDown animate');
                   }, 480);
               });

               $card.mouseenter(function() {
                   const $this = $(this);
                   const hover_count = parseInt($this.attr('data-count'), 10) + 1 || 0;
                   $this.attr('data-count', hover_count);
                   if (hover_count >= 20) {
                       $(this).children('.card-header, .card-image').addClass('hinge animated');
                   }
               });
           });
       }
   }

   roundToNearestTenUp(num: number) {
    return ((Math.round(num / 10) + 1) * 10);
  }



}
