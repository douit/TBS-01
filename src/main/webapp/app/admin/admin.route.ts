import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot, Routes} from '@angular/router';

import { auditsRoute } from './audits/audits.route';
import { configurationRoute } from './configuration/configuration.route';
import { docsRoute } from './docs/docs.route';
import { healthRoute } from './health/health.route';
import { logsRoute } from './logs/logs.route';
import { metricsRoute } from './metrics/metrics.route';
import { userMgmtRoute } from './user-management/user-management.route';

import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import {UserMgmtComponent} from 'app/admin/user-management/user-management.component';
import {JhiResolvePagingParams} from 'ng-jhipster';
import {UserMgmtUpdateComponent} from 'app/admin/user-management/user-management-update.component';
// import {UserMgmtResolve} from 'app/admin/user-management/user-management.route';
import {User} from 'app/core/user/user.model';
import {UserService} from 'app/core/user/user.service';
import {Injectable} from '@angular/core';

const ADMIN_ROUTES = [auditsRoute, configurationRoute, docsRoute, healthRoute, logsRoute, ...userMgmtRoute, metricsRoute];

@Injectable({ providedIn: 'root' })
export class UserMgmtResolve implements Resolve<any> {
  constructor(private service: UserService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const id = route.params['login'] ? route.params['login'] : null;
    if (id) {
      return this.service.find(id);
    }
    return new User();
  }
}

export const adminState: Routes = [
  {
    path: '',
    data: {
      authorities: ['ROLE_ADMIN']
    },
    canActivate: [UserRouteAccessService],
    // children: ADMIN_ROUTES
    children: [
      {
        path: 'user-management',
        component: UserMgmtComponent,
        data: {
          authorities: ['ROLE_ADMIN'],
          pageTitle: 'userManagement.home.title'
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          pagingParams: JhiResolvePagingParams
        }
      },
      {
        path: 'user-management/new',
        component: UserMgmtUpdateComponent,
        data: {
          authorities: ['ROLE_ADMIN'],
          pageTitle: 'userManagement.home.createLabel'
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          user: UserMgmtResolve
        }
      },
      {
        path: 'user-management/:login/edit',
        component: UserMgmtUpdateComponent,
        data: {
          authorities: ['ROLE_ADMIN'],
          pageTitle: 'userManagement.home.editLabel'
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          user: UserMgmtResolve
        }
      }
    ]
  }
];
