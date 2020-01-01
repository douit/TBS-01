import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { JhiLanguageHelper } from 'app/core/language/language.helper';
import { User } from 'app/core/user/user.model';
import { UserService } from 'app/core/user/user.service';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {IClient} from 'app/shared/model/client.model';
import {filter, map} from 'rxjs/operators';
import {ClientService} from 'app/client/client.service';
import {JhiAlertService} from 'ng-jhipster';

@Component({
  selector: 'app-user-mgmt-update',
  templateUrl: './user-management-update.component.html'
})
export class UserMgmtUpdateComponent implements OnInit {
  user: User;
  languages: any[];
  authorities: any[];
  clients: IClient[];
  filtredClients: IClient[];
  isSaving: boolean;
  isCreate: boolean;

  validTextType = false;
  validEmailType = false;

  role: any = {};
  roleClient: string;
  roleName: string;
  roles: any [] = [];

  editForm = this.fb.group({
    id: [null],
    login: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50), Validators.pattern('^[_.@A-Za-z0-9-]*')]],
    firstName: ['', [Validators.maxLength(50)]],
    lastName: ['', [Validators.maxLength(50)]],
    email: ['', [Validators.minLength(5), Validators.maxLength(254), Validators.email]],
    activated: [true],
    langKey: [],
    authorities: [],
    roleClient: [],
    roleName: []
  });

  constructor(
    private languageHelper: JhiLanguageHelper,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    protected clientService: ClientService,
    protected jhiAlertService: JhiAlertService
  ) {}

  displayFieldCss(form: FormGroup, field: string) {
    return {
      'has-error': this.isFieldValid(form, field),
      'has-feedback': this.isFieldValid(form, field)
    };
  }

  isFieldValid(form: FormGroup, field: string) {
    return !form.get(field).valid && form.get(field).touched;
  }

  ngOnInit() {
    this.isSaving = false;
    this.route.data.subscribe(({ user }) => {
      this.user = user.body ? user.body : user;
      if (user.body) {
        this.user = user.body;
        this.isCreate = false;
      } else {
        this.user = user;
        this.isCreate = true;
      }
      this.updateForm(this.user);
    });
    this.authorities = [];
    this.userService.authorities().subscribe(authorities => {
      this.authorities = authorities;
    });
    this.languageHelper.getAll().then(languages => {
      this.languages = languages;
    });
    this.clientService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IClient[]>) => mayBeOk.ok),
        map((response: HttpResponse<IClient[]>) => response.body)
      )
      .subscribe((res: IClient[]) => (this.initClientsAndExistingRoles(res)), (res: HttpErrorResponse) => this.onError(res.message));
  }

  private initClientsAndExistingRoles(res: IClient[]): void {
    this.clients = res;
    this.filtredClients = res;
    if (!this.isCreate) {
      this.user.clientRoles.forEach(clientRole => {
        this.role.roleClient = this.clients.filter(client => client.id === clientRole.clientId)[0].name;
        this.role.clientId = clientRole.clientId;
        this.role.roleName = clientRole.roleName;

        this.roles.splice(this.roles.length, 0, this.role);
        this.role = {};
      });
      this.filterClients();
    }
  }

  private filterClients(): void {
    this.filtredClients = this.clients.filter(client => {
      let clientNotAdded = true;
      this.roles.forEach(role => {
        if (role.clientId === client.id) {
          clientNotAdded = false;
          return ;
        }
      });
      return clientNotAdded;
    });
    // console.log('filtredClients: ' + JSON.stringify(this.filtredClients));
  }

  private updateForm(user: User): void {
    this.editForm.patchValue({
      id: user.id,
      login: user.login,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      activated: user.activated,
      langKey: user.langKey,
      authorities: user.authorities
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    this.updateUser(this.user);
    if (this.user.id !== null) {
      this.userService.update(this.user).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
    } else {
      this.userService.create(this.user).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
    }
  }

  private updateUser(user: User): void {
    user.login = this.editForm.get(['login']).value;
    user.firstName = this.editForm.get(['firstName']).value;
    user.lastName = this.editForm.get(['lastName']).value;
    user.email = this.editForm.get(['email']).value;
    user.activated = this.editForm.get(['activated']).value;
    user.langKey = this.editForm.get(['langKey']).value;
    user.authorities = this.editForm.get(['authorities']).value;
    user.clientRoles = this.roles;
  }

  private onSaveSuccess(result) {
    this.isSaving = false;
    this.previousState();
  }

  private onSaveError() {
    this.isSaving = false;
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  addNewRole() {
    if (!this.roleClient || !this.roleName) {
      this.jhiAlertService.error('userManagement.error.selectClientAndRole', null, null);
      return;
    }
    this.role.roleClient = this.editForm.controls['roleClient'].value.name;
    this.role.clientId = this.editForm.controls['roleClient'].value.id;
    this.role.roleName = this.editForm.controls['roleName'].value;

    this.roles.splice(this.roles.length, 0, this.role);
    this.role = {};
    this.roleClient = null;
    this.roleName = null;
    this.editForm.patchValue({
      roleClient : '',
      roleName : ''
    });
    console.log('roles1: ' + JSON.stringify(this.roles));
    this.filterClients();
    console.log('roles2: ' + JSON.stringify(this.roles));
  }

  delete(id: number, index: number) {
    /*if (id) {
      this.roles.filter((t) => {
        if (t.id === id) {
          return t;
        }
      })[0].roleClient = -1; // -1 means the item has been deleted
    } else {*/
    this.roles.splice(index, 1);
    this.filterClients();
    // }
  }

  clientRolesNotEmpty() {
    return this.roles.filter(t => {
      if (t.id !== -1) {
        return t;
      }
    }).length > 0;
  }
}
