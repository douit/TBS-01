import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {NotificationComponent} from '../../shared/notification/notification.component';
import {ReportService} from '../report.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {_tbs} from '../../shared/util/tbs-utility';
import {ZonedDateTime} from 'js-joda';

declare var $: any;

@Component({
    selector: 'app-payment-reports',
    templateUrl: './payment-report.component.html',
    styleUrls: ['./payment-report.component.css']
})

export class PaymentReportComponent implements OnInit {

    table: any;
    dtOptions = {};
    public processing: boolean = true;
    searchInput: string = '';

    public daterange: any = {};

    public options: any = {
        locale: {format: 'YYYY-MM-DD'},
        alwaysShowCalendars: false,
        autoApply: true,
        opens: 'center',
        timePicker: true,
        timePicker24Hour: true,
        autoUpdateInput: true,
        startDate: new Date(new Date().setMonth(new Date().getMonth() - 1)),
        endDate: new Date(),
        maxDate: new Date()
    };

    dropdownSettings = {};

    reportForm: FormGroup;

    constructor(private http: HttpClient,
                private router: Router,
                private notification: NotificationComponent,
                private reportService: ReportService) {
    }

    ngOnInit() {

        this.daterange = {
            start: this.options.startDate,
            end: this.options.endDate
        };

        this.reportForm = new FormGroup({
            driverId: new FormControl('', [Validators.min(0)]),
            city: new FormControl(''),
            dateRangeInput: new FormControl('')
        });

        this.dropdownSettings = {
            singleSelection: true,
            idField: 'id',
            textField: 'name',
            itemsShowLimit: 3,
            allowSearchFilter: true,
            closeDropDownOnSelection: true
        };

        /*this.reportService.getCities().subscribe((res) => {
            let arr = [];
            arr = (<any>Object).values(res);
            this.cities = arr.map(c => new City(c.name, c.id, c.code));
        });*/


        const that = this;

        setInterval(
            function () {
                that.table.draw(false);
            }, 30000);

        this.dtOptions = {
            ajax: {
                url: '/api/v1/report/driver/datatable'
            },
            oSearch: {"sSearch": that.searchInput},
            columns: [
                {
                    title: 'Report ID',
                    data: 'id',
                    visible: true,
                    orderable: false,
                    sortable: true,
                    searchable: true
                }, {
                    title: 'Request User Id',
                    data: 'requestUserId',
                    visible: false,
                    orderable: false,
                    sortable: false,
                    searchable: false
                }, {
                    title: 'Type',
                    data: 'type',
                    visible: false,
                    orderable: false,
                    sortable: false,
                    searchable: false
                }, {
                    title: 'Request Date',
                    orderable: true,
                    sortable: true,
                    searchable: false,
                    data: 'requestDate',
                    render: function (data) {
                        if (data) {
                            return '_tbs.formatDate(data)';
                        } else {
                            return 'Not Ready Yet';
                        }
                    },
                    visible: true
                }, {
                    title: 'Generated Date',
                    orderable: false,
                    sortable: false,
                    searchable: false,
                    data: 'generatedDate',
                    render: function (data) {
                        if (data) {
                            return '_tbs.formatDate(data)';
                        } else {
                            return 'Not Ready Yet';
                        }
                    },
                    visible: true
                }, {
                    title: 'Expire Date',
                    orderable: false,
                    sortable: false,
                    searchable: false,
                    data: 'expireDate',
                    render: function (data) {
                        if (data) {
                            return '_tbs.formatDate(data)';
                        } else {
                            return 'Not Ready Yet';
                        }
                    },
                    visible: true
                }, {
                    title: 'Status',
                    orderable: false,
                    sortable: false,
                    searchable: false,
                    data: 'status',
                    render: function (data) {

                        let statusDiv: HTMLDivElement;
                        statusDiv = document.createElement('div');
                        let statusSpan: HTMLSpanElement;
                        statusSpan = document.createElement('span');
                        statusSpan.setAttribute('class', 'label');
                        statusSpan.innerHTML = '_tbs.humanizeEnumString(data)';
                        statusDiv.appendChild(statusSpan);
                        switch (data) {
                            case "WAITING":
                                statusSpan.classList.add('label-warning');
                                statusSpan.style.marginRight = "5px";
                                let loading: HTMLElement;
                                loading = document.createElement('i');
                                loading.setAttribute('class', "fa fa-spinner fa-spin");
                                loading.setAttribute('data-toggle', "tooltip");
                                loading.setAttribute('data-placement', "top");
                                loading.setAttribute('title', "Generating..!");
                                loading.style.color = "#FCB220";
                                statusDiv.appendChild(loading);
                                break;
                            case "READY":
                                statusSpan.classList.add('label-success');
                                break;
                            default:
                                statusSpan.classList.add('label-info');
                        }
                        statusSpan.style.width = '100%';
                        return statusDiv.outerHTML;
                    },
                    visible: true
                }, {
                    title: 'Download',
                    orderable: false,
                    sortable: false,
                    searchable: false,
                    visible: true,
                    data: function (data) {
                        let downloadDiv: HTMLDivElement;
                        downloadDiv = document.createElement('div');

                        let downloadAncher: HTMLElement;
                        downloadAncher = document.createElement('a');
                        if (data.status == "READY") {
                            downloadAncher.setAttribute('href', data.downloadUrl);
                        }
                        let downloadSpan: HTMLElement;
                        downloadSpan = document.createElement('i');
                        downloadSpan.setAttribute('class', 'fa fa-download');
                        downloadSpan.setAttribute('download', data.downloadUrl);
                        downloadAncher.appendChild(downloadSpan);
                        downloadDiv.appendChild(downloadAncher);

                        return downloadDiv.outerHTML;

                    }
                }
            ],
            ordering: true,
            order: [[3, "desc"]],
            lengthChange: false,
            pageLength: 10,
            processing: false,
            serverSide: true,
            autoWidth: false,
            dom: '<"top"i>rt<"bottom"p><"clear">'
        };
    }

    public globalFilter() {
        this.table.search(this.searchInput).draw();
    }

    public clearFilter() {
        this.searchInput = '';
        this.globalFilter();
    }

    generateReport() {
        if (this.reportForm.valid) {
            this.processing = true;
            let cityId = null;
           /* if (this.selectedCity && this.selectedCity.length > 0) {
                cityId = this.selectedCity[0].id;
            }*/

            let newDaterange: any = {};
            if (this.daterange) {
                newDaterange = this.daterange;
            }

            const paymentReportRequest = {
                driverId: this.reportForm.controls['driverId'].value,
                cityId: cityId,
                startDate: newDaterange.start,
                endDate: newDaterange.end,
                offset: ZonedDateTime.now().offset()._id
            };


            this.reportService.requestPaymentReport(paymentReportRequest).subscribe(
                data => {
                    this.processing = false;
                    this.reset();
                    this.notification.showNotification('success', 'Report Requested Successfully');
                    this.table.draw(false);

                },
                err => {
                    this.processing = false;
                    this.table.draw(false);
                    for (const field of err.error.errors) {
                        this.notification.showNotification('danger', field.message);
                    }
                }
            );
        } else {
            this.notification.showNotification('danger', "please fill all the required fields");
        }

    }

    reset() {
        // this.selectedCity = null;
        this.reportForm.reset();
    }

    public selectedDate(value: any) {
        this.daterange.start = value.start._d;
        this.daterange.end = value.end._d;
    }

    isFieldValid(form: FormGroup, field: string) {
        return !form.get(field).valid && form.get(field).touched;
    }

}
