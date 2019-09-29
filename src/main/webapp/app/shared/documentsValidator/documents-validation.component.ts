import {Component} from '@angular/core';
import {NotificationComponent} from '../notification/notification.component';

/**
 * Created by oalbader on 05/03/2018.
 */

declare var $: any;

@Component({
    selector: 'app-documents-validation',
    templateUrl: 'documents-validation.component.html',
})
export class DocumentsValidationComponent {
    constructor(private notification: NotificationComponent){}

    isDocumentValid (file) {
        const maxSize = 10000000; // 10MB
        const allowedExtensions = ['jpg', 'png', 'jpeg', 'gif', 'xlsx', 'csv', 'doc', 'docx', 'pdf'];
        let isValid = true;

        if (file.files.length > 0) {
            const documentSize = file.files[0].size;
            const documentType = file.files[0].name.split('.').pop().toLowerCase();
            if (documentSize > maxSize) {
                isValid = false;
                this.notification.showNotification('danger', 'Document Size Should not exceed 10MB');
            } else if (allowedExtensions.indexOf(documentType) < 0) {
                isValid = false;
                this.notification.showNotification('danger', 'Document Type should be in these types : [jpej, jpg, gif, png, doc, docx, xlsx, csv, pdf]');
            } else {
                isValid = true;
            }
        }
        return isValid;
    }
}
