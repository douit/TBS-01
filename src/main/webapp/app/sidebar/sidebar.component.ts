import { Component, OnInit } from '@angular/core';
import PerfectScrollbar from 'perfect-scrollbar';
import { AccountService } from 'app/core/auth/account.service';
// declare const $: any;
import * as $ from 'jquery';
import {AuthConsts} from 'app/shared/auth-consts';
import {type} from 'os';
import {LoginService} from 'app/core/login/login.service';
import {Router} from '@angular/router';
import {translate} from "googleapis/build/src/apis/translate";
import {TranslateService} from "@ngx-translate/core";

// Metadata
export interface RouteInfo {
    path: string;
    title: string;
    type: string;
    icontype: string;
    collapse?: string;
    authorities?: string[];
    children?: ChildrenItems[];
}

export interface ChildrenItems {
    path: string;
    title: string;
    ab: string;
    icontype: string;
    type?: string;
    authorities?: string[];
}

@Component({
    selector: 'app-sidebar-cmp',
    templateUrl: 'sidebar.component.html',
})

export class SidebarComponent implements OnInit {
    public menuItems: any[];
    ps: any;
    public username: string;
    public avatar: string;
    private ROUTES: RouteInfo[] = [
    {
      path: '/dashboard',
      title: this.translateService.instant('tbsApp.sidebar.dashboard'),
      authorities: [],
      type: 'link',
      icontype: 'dashboard'
    }, {
    path: '/item',
      title: this.translateService.instant('tbsApp.sidebar.items'),
      authorities: [AuthConsts.VIEW_ITEM],
      type: 'link',
      icontype: 'grid_on'
    }, {
      path: '/invoice',
        title: this.translateService.instant('tbsApp.sidebar.invoices'),
        authorities: [AuthConsts.VIEW_INVOICE],
        type: 'link',
        icontype: 'content_paste'
    }, {
      path: '/payment',
        title:this.translateService.instant('tbsApp.sidebar.payments'),
        authorities: [AuthConsts.VIEW_PAYMENT],
        type: 'link',
        icontype: 'attach_money'
    }, {
      path: '/customer/test_cc',
        title:this.translateService.instant('tbsApp.sidebar.testcreditcardpayment'),
        authorities: [],
        type: 'link',
        icontype: 'widgets'
    }, {
        path: '/report',
        title: this.translateService.instant('tbsApp.sidebar.reports'),
        authorities: [AuthConsts.VIEW_REPORT],
        type: 'sub',
        icontype: 'timeline',
        collapse: 'report',
        children: [
          {
            path: 'payment-report',
            title: this.translateService.instant('tbsApp.sidebar.payments'),
            authorities: [AuthConsts.VIEW_REPORT],
            icontype: 'list',
            ab: ''
          },
          {
            path: 'refund-report',
            title: this.translateService.instant('tbsApp.sidebar.refunds'),
            authorities: [AuthConsts.VIEW_REPORT],
            icontype: 'list',
            ab: ''
          }
        ]
    },
    {
      path: '/admin',
        title: this.translateService.instant('tbsApp.sidebar.administrative'),
        authorities: [AuthConsts.VIEW_USER],
        type: 'sub',
        icontype: 'settings',
        collapse: 'admin',
        children: [
        {path: 'user-management',
          title: this.translateService.instant('tbsApp.sidebar.users'),
          authorities: [AuthConsts.VIEW_USER],
          icontype: 'list',
          ab: ''}
      ]
    }
      /*  , {
          path: '/components',
          title: 'Components',
          type: 'sub',
          icontype: 'apps',
          collapse: 'components',
          children: [
              {path: 'buttons', title: 'Buttons', ab:''},
              {path: 'grid', title: 'Grid System', ab:''},
              {path: 'panels', title: 'Panels', ab:''},
              {path: 'sweet-alert', title: 'Sweet Alert', ab:''},
              {path: 'notifications', title: 'Notifications', ab:''},
              {path: 'icons', title: 'Icons', ab:''},
              {path: 'typography', title: 'Typography', ab:''}
          ]
      }, {
          path: '/forms',
          title: 'Forms',
          type: 'sub',
          icontype: 'content_paste',
          collapse: 'forms',
          children: [
              {path: 'regular', title: 'Regular Forms', ab:''},
              {path: 'extended', title: 'Extended Forms', ab:''},
              {path: 'validation', title: 'Validation Forms', ab:''},
              {path: 'wizard', title: 'Wizard', ab:''}
          ]
      }, {
          path: '/tables',
          title: 'Tables',
          type: 'sub',
          icontype: 'grid_on',
          collapse: 'tables',
          children: [
              {path: 'regular', title: 'Regular Tables', ab:''},
              {path: 'extended', title: 'Extended Tables', ab:''},
              {path: 'datatables.net', title: 'Datatables.net', ab:''}
          ]
      }, {
          path: '/maps',
          title: 'Maps',
          type: 'sub',
          icontype: 'place',
          collapse: 'maps',
          children: [
              {path: 'google', title: 'Google Maps', ab:''},
              {path: 'fullscreen', title: 'Full Screen Map', ab:''},
              {path: 'vector', title: 'Vector Map', ab:''}
          ]
      }, {
          path: '/widgets',
          title: 'Widgets',
          type: 'link',
          icontype: 'widgets'

      }, {
          path: '/charts',
          title: 'Charts',
          type: 'link',
          icontype: 'timeline'

      }, {
          path: '/calendar',
          title: 'Calendar',
          type: 'link',
          icontype: 'date_range'
      }, {
          path: '/pages',
          title: 'Pages',
          type: 'sub',
          icontype: 'image',
          collapse: 'pages',
          children: [
              {path: 'pricing', title: 'Pricing', ab:''},
              {path: 'timeline', title: 'Timeline Page', ab:''},
              {path: 'login', title: 'Login Page', ab:'LP'},
              {path: 'register', title: 'Register Page', ab:''},
              {path: 'lock', title: 'Lock Screen Page', ab:''},
              {path: 'user', title: 'User Page', ab:''}
          ]
      }*/
  ];

  constructor(private accountService: AccountService,
              private router: Router,
              private loginService: LoginService,
              private translateService: TranslateService) {
  }

    isMobileMenu() {
        if ($(window).width() > 991) {
            return false;
        }
        return true;
    }

    ngOnInit() {
      // this.menuItems = ROUTES.filter(menuItem => menuItem);
      this.menuItems = this.sanitizeMenuItems(this.ROUTES);
      if (window.matchMedia(`(min-width: 960px)`).matches && !this.isMac()) {
          const elemSidebar = <HTMLElement>document.querySelector('.sidebar .sidebar-wrapper');
          this.ps = new PerfectScrollbar(elemSidebar);
      }
      this.accountService.identity().then(account => {
        this.username = account.firstName + ' ' + account.lastName;
        if (account.imageUrl != null && account.imageUrl !== '') {
          this.avatar = account.imageUrl;
        } else {
          this.avatar = '/content/img/default-avatar.png';
        }
      });

    }
    sanitizeMenuItems(menuItems: RouteInfo[]): RouteInfo[] {
      return menuItems.filter(menuItem => {
        let childes = [];

        if (menuItem.children !== undefined) {
          childes = menuItem.children.filter(child => {
            if (child.authorities === undefined || child.authorities.length === 0) {
              return true;
            }
            return this.accountService.hasAnyAuthority(child.authorities);
          });
        }

        menuItem.children = childes;
        // if the user permit access to a child route
        // then the parent route access should be granted
        if (childes.length > 0) {
          return true;
        }

        if (menuItem.authorities === undefined || menuItem.authorities.length === 0) {
          return true;
        }

        return this.accountService.hasAnyAuthority(menuItem.authorities);
      });
    }
    updatePS(): void  {
        if (window.matchMedia(`(min-width: 960px)`).matches && !this.isMac()) {
            this.ps.update();
        }
    }
    isMac(): boolean {
        let bool = false;
        if (navigator.platform.toUpperCase().indexOf('MAC') >= 0 || navigator.platform.toUpperCase().indexOf('IPAD') >= 0) {
            bool = true;
        }
        return bool;
    }
  logout() {
    this.loginService.logout();
    this.router.navigate(['']);
  }
}
