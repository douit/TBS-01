import { Injectable } from '@angular/core';
import { AccountService } from '../auth/account.service';
import { AuthServerProvider } from '../auth/auth-session.service';
import {Router} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class LoginService {
  constructor(private accountService: AccountService,
              private authServerProvider: AuthServerProvider,
              private router: Router,) {}

  login(credentials, callback?) {
    const cb = callback || function() {};

    return new Promise((resolve, reject) => {
      this.authServerProvider.login(credentials).subscribe(
        data => {
          this.accountService.identity(true).then(account => {
            resolve(data);
          });
          return cb();
        },
        err => {
          this.logout();
          reject(err);
          return cb(err);
        }
      );
    });
  }

  logout() {
    this.authServerProvider.logout().subscribe(null, null,
      () => {
        this.accountService.authenticate(null);
        this.router.navigate(['login']);
      }
      );
  }
}
