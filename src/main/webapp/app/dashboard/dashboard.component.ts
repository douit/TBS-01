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


// declare const $: any;

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, AfterViewInit {
  // constructor(private navbarTitleService: NavbarTitleService, private notificationService: NotificationService) { }
  dashboardService: DashboardService;
  statistics: IStatistics = {};
  chartStatistics: IChartStatistics [] = [];

  dataMonthlyChart = {
    labels: []
    ,
    series: []
  };
  dataAnnualChart = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    series: []
  };

  constructor(private http: HttpClient, dashboardService: DashboardService ) {
    this.dashboardService = dashboardService;
  }
  public tableData: TableData;
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
            this.dataMonthlyChart.labels.push(statMonth.day);
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
    date :  moment(),
     type: TypeStatistics.MONTHLY
    };

    const chartAnnualStatisticsRequest: IChartStatisticsRequest = {
      date :  moment(),
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
