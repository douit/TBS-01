import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ITax } from 'app/shared/model/tax.model';
import { TaxService } from './tax.service';

@Component({
  selector: 'jhi-tax-delete-dialog',
  templateUrl: './tax-delete-dialog.component.html'
})
export class TaxDeleteDialogComponent {
  tax: ITax;

  constructor(protected taxService: TaxService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.taxService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'taxListModification',
        content: 'Deleted an tax'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-tax-delete-popup',
  template: ''
})
export class TaxDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ tax }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(TaxDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.tax = tax;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/tax', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/tax', { outlets: { popup: null } }]);
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
