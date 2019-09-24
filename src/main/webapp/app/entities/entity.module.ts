import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'item',
        loadChildren: () => import('./item/item.module').then(m => m.TbsItemModule)
      },
      {
        path: 'tax',
        loadChildren: () => import('./tax/tax.module').then(m => m.TbsTaxModule)
      },
      {
        path: 'category',
        loadChildren: () => import('./category/category.module').then(m => m.TbsCategoryModule)
      },
      {
        path: 'invoice',
        loadChildren: () => import('./invoice/invoice.module').then(m => m.TbsInvoiceModule)
      },
      {
        path: 'invoice-item',
        loadChildren: () => import('./invoice-item/invoice-item.module').then(m => m.TbsInvoiceItemModule)
      },
      {
        path: 'discount',
        loadChildren: () => import('./discount/discount.module').then(m => m.TbsDiscountModule)
      },
      {
        path: 'payment',
        loadChildren: () => import('./payment/payment.module').then(m => m.TbsPaymentModule)
      },
      {
        path: 'payment-method',
        loadChildren: () => import('./payment-method/payment-method.module').then(m => m.TbsPaymentMethodModule)
      },
      {
        path: 'refund',
        loadChildren: () => import('./refund/refund.module').then(m => m.TbsRefundModule)
      },
      {
        path: 'client',
        loadChildren: () => import('./client/client.module').then(m => m.TbsClientModule)
      }
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ])
  ],
  declarations: [],
  entryComponents: [],
  providers: []
})
export class TbsEntityModule {}
