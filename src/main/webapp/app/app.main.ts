import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { ProdConfig } from './blocks/config/prod.config';
import { TbsAppModule } from './app.module';

ProdConfig();

if (module['hot']) {
  module['hot'].accept();
}

platformBrowserDynamic()
  .bootstrapModule(TbsAppModule, { preserveWhitespaces: true })
  // eslint-disable-next-line no-console
  .then(success => console.log('Application started'))
  .catch(err => console.error(err));
