import { Injectable } from '@angular/core';
import { JhiEventManager } from 'ng-jhipster';
import { HttpInterceptor, HttpRequest, HttpErrorResponse, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class ErrorHandlerInterceptor implements HttpInterceptor {
  constructor(private eventManager: JhiEventManager) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      tap(
        (event: HttpEvent<any>) => {},
        (err: any) => {

          if (err instanceof HttpErrorResponse) {
            if (!(
                  (err.status === 401 || err.status === 503 || err.status === 504)
                  && (err.message === '' || (err.url && err.url.includes('api/account')) || (err.url && err.url.includes('/datatable?columns')))
            )) {
              this.eventManager.broadcast({ name: 'tbsApp.httpError', content: err });
            }
          }

          // fix new deploy issue as the generated file name will be different from the local cache
          const chunkFailedMessage = /Loading chunk [\d]+ failed/;
          if (chunkFailedMessage.test(err.message)) {
            window.location.reload();
          }

        }
      )
    );
  }
}
