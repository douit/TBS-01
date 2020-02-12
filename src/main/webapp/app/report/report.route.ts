import {Routes} from '@angular/router';
import {AuthConsts} from '../shared/auth-consts';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import {PaymentReportComponent} from './payment-report/payment-report.component';
import {RefundReportComponent} from 'app/report/refund-report/refund-report.component';

export const reportRoutes: Routes = [
    {
        path: '',
        data: {
          authorities: [AuthConsts.VIEW_REPORT]
        },
        canActivate: [UserRouteAccessService],
        children: [
          {
              path: 'payment-report',
              component: PaymentReportComponent,
              data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'report.paymentReport'
              },
              canActivate: [UserRouteAccessService]
          },
          {
              path: 'refund-report',
              component: RefundReportComponent,
              data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'report.refundReport'
              },
              canActivate: [UserRouteAccessService]
          }
        ]
    }
];


