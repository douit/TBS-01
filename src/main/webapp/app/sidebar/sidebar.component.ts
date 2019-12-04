import { Component, OnInit } from '@angular/core';
import PerfectScrollbar from 'perfect-scrollbar';
import { AccountService } from 'app/core/auth/account.service';
// declare const $: any;
import * as $ from 'jquery';

// Metadata
export interface RouteInfo {
    path: string;
    title: string;
    type: string;
    icontype: string;
    collapse?: string;
    children?: ChildrenItems[];
}

export interface ChildrenItems {
    path: string;
    title: string;
    ab: string;
    type?: string;
}

// Menu Items
export const ROUTES: RouteInfo[] = [{
        path: '/dashboard',
        title: 'Dashboard',
        type: 'link',
        icontype: 'dashboard'
    }, {
        path: '/item',
        title: 'Items',
        type: 'link',
        icontype: 'grid_on'
    }, {
        path: '/invoice',
        title: 'Invoices',
        type: 'link',
        icontype: 'content_paste'
    }, {
        path: '/payment',
        title: 'Payments',
        type: 'link',
        icontype: 'attach_money'
    }, {
        path: '/customer/test_cc',
        title: 'Test Credit Card Payment',
        type: 'link',
        icontype: 'widgets'
}, {
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
    },{
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
    }
];
@Component({
    selector: 'app-sidebar-cmp',
    templateUrl: 'sidebar.component.html',
})

export class SidebarComponent implements OnInit {
    public menuItems: any[];
    ps: any;
    public username: string;
    public avatar: string;

  constructor(private accountService: AccountService) {
  }

    isMobileMenu() {
        if ($(window).width() > 991) {
            return false;
        }
        return true;
    }

    ngOnInit() {
      this.menuItems = ROUTES.filter(menuItem => menuItem);
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
}
