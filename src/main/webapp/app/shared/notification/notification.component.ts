import {Component} from '@angular/core';
/**
 * Created by oalbader on 04/03/2018.
 */
declare var $: any;

@Component({
    selector: 'app-notification',
    templateUrl: 'notification.component.html',
})
export class NotificationComponent {
    // notification types ['', 'info', 'success', 'warning', 'danger', 'rose', 'primary']
    showNotification(type, message) {

        $.notify({
            message: message
        }, {
            type: type,
            timer: 4000,
            placement: {
                from: 'top',
                align: 'right'
            }
        });
    }
}
