import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IRefund } from 'app/shared/model/refund.model';
import { RefundService } from './refund.service';

@Component({
  selector: 'jhi-refund-delete-dialog',
  templateUrl: './refund-delete-dialog.component.html'
})
export class RefundDeleteDialogComponent {
  refund: IRefund;

  constructor(protected refundService: RefundService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.refundService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'refundListModification',
        content: 'Deleted an refund'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-refund-delete-popup',
  template: ''
})
export class RefundDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ refund }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(RefundDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.refund = refund;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/refund', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/refund', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          }
        );
      }, 0);
    });
  }

  ngOnDestroy() {
    this.ngbModalRef = null;
  }
}
