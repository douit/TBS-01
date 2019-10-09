import {Component, OnInit} from '@angular/core';
import PerfectScrollbar from 'perfect-scrollbar';
/*import {AuthenticationService} from "../shared/authentication/authentication-service.service";
import {Principle} from "../shared/authentication/Principle";*/
import { AccountService } from 'app/core/auth/account.service';
import {SidebarService} from "./sidebar.service";
import {AuthConsts} from "../shared/auth-consts";

declare const $: any;

//Metadata
export interface RouteInfo {
    path: string;
    title: string;
    type: string;
    icontype: string;
    collapse?: string;
    authorities?: string[]
    children?: ChildrenItems[];
}

export interface ChildrenItems {
    path: string;
    title: string;
    // ab?: string;
    icontype: string;
    type?: string;
    authorities?: string[]
}

//Menu Items
export const ROUTES: RouteInfo[] = [{
  path: '/dashboard',
  title: 'Dashboard',
  authorities: ['ROLE_USER'],
  type: 'link',
  icontype: 'dashboard'
}
, {
  path: '/administrative',
  authorities: ['ROLE_USER'],
  title: 'Administrative',
  type: 'link',
  icontype: 'settings',
  collapse: 'administrative'
}
];

@Component({
    selector: 'app-sidebar-cmp',
    templateUrl: 'sidebar.component.html',
    styleUrls: ['sidebar.component.scss']
})

export class SidebarComponent implements OnInit {
    public menuItems: any[];
    public username: string;
    public avatar: string;
    // public user: Principle;

    isMobileMenu() {
        if ($(window).width() > 991) {
            return false;
        }
        return true;
    };

    constructor(/*private authenticationService: AuthenticationService,*/
                private accountService: AccountService,
                private sidebarService: SidebarService) {
    }

    ngOnInit() {
        this.sidebarService.currentAvatar.subscribe(avatar => this.avatar = avatar);
        this.menuItems = this.sanitizeMenuItems(ROUTES);
        //this.user = this.authenticationService.getUser();
      this.accountService.identity().then(account => {
        this.username = account.firstName + ' ' + account.lastName;
        if(account.imageUrl != null && account.imageUrl != "") {
          this.avatar = account.imageUrl;
        } else {
          this.avatar = '../../content/images/default-avatar.png';
        }
      });
        /*this.username = this.user.username;
        if (this.user.avatar != null) {
            this.avatar = '/api/v1/aws/s3/download?key=' + this.user.avatar.path;
        } else {
            this.avatar = '../assets/img/default-avatar.png';
        }*/

    }

    sanitizeMenuItems(menuItems: RouteInfo[]): RouteInfo[] {
        return menuItems.filter(menuItem => {
            let childes = [];

            if (menuItem.children !== undefined) {
                childes = menuItem.children.filter(child => {
                    if (child.authorities === undefined || child.authorities.length === 0) {
                        return true;
                    }
                    // return this.authenticationService.hasAnyAuthorities(child.authorities);
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

            // return this.authenticationService.hasAnyAuthorities(menuItem.authorities);
          return this.accountService.hasAnyAuthority(menuItem.authorities);
        });
    }

    updatePS(): void {
        if (window.matchMedia(`(min-width: 960px)`).matches && !this.isMac()) {
            const elemSidebar = <HTMLElement>document.querySelector('.sidebar .sidebar-wrapper');
            let ps = new PerfectScrollbar(elemSidebar, {wheelSpeed: 2, suppressScrollX: true});
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
