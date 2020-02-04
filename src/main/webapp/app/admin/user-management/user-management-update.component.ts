import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';

import {JhiLanguageHelper} from 'app/core/language/language.helper';
import {User} from 'app/core/user/user.model';
import {UserService} from 'app/core/user/user.service';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {IClient} from 'app/shared/model/client.model';
import {filter, map} from 'rxjs/operators';
import {ClientService} from 'app/client/client.service';
import {JhiAlertService} from 'ng-jhipster';
import {TranslateService} from "@ngx-translate/core";
import {any} from "codelyzer/util/function";

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
  text: string[];
  translateAuthorities: string [] = [];
  role: any = {};
  roleClient: string;
  roleName: string;
  roles: any [] = [];
  isInternal: boolean = true;
  ldapDateEmpty: boolean = true;
  IsFieldsEmpty: boolean = true;


  editForm = this.fb.group({
    id: [null],
    login: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50), Validators.pattern('^[_.@A-Za-z0-9-]*')]],
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email]],
    activated: [true],
    langKey: [],
    authorities: [],
    roleClient: [],
    roleName: [],
    isInternal: [true],
    search: ['']
    // , Validators.required
  });

  constructor(
    private languageHelper: JhiLanguageHelper,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    protected clientService: ClientService,
    protected jhiAlertService: JhiAlertService,
    private translateService: TranslateService
  ) {
  }

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
    this.route.data.subscribe(({user}) => {
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
    const searchControl = this.editForm.get('search');
    if (this.isInternal == true && !this.user.id) {
      searchControl.setValidators([Validators.required]);
    }
    this.authorities = [];
    this.userService.authorities().subscribe(authorities => {
      this.authorities = authorities;
    });
    this.languageHelper.getAll().then(languages => {
      this.languages = languages;
    });
    this.clientService.getClientByRole()
      .subscribe(
        res => {
          this.initClientsAndExistingRoles(res.body);
        },
        res => {
          console.log('An error has occurred when get clientByRole');
        }
      );
    // this.clientService
    //   .query()
    //   .pipe(
    //     filter((mayBeOk: HttpResponse<IClient[]>) => mayBeOk.ok),
    //     map((response: HttpResponse<IClient[]>) => response.body)
    //   )
    //   .subscribe((res: IClient[]) => (this.initClientsAndExistingRoles(res)), (res: HttpErrorResponse) => this.onError(res.message));

    if (this.user.internal || !this.user.id) {
      this.editForm.controls['login'].disable();
      this.editForm.controls['firstName'].disable();
      this.editForm.controls['lastName'].disable();
      this.editForm.controls['email'].disable();
    }
    if (this.user.clientRoles != null) {
      const that = this;
      this.user.clientRoles.forEach(function (value) {

        that.userService.getRoleAuthorities(value.roleName.toString()).subscribe(
          res => {
            that.text = res;

            that.text.forEach(element => {
              that.translateAuthorities.push(that.translateService.instant('userManagement.authorities.' + element) + "<br>");
            });
          },
          res => {
            console.log('An error has occurred when get role authorities');
          }
        );
      });
    }
  }

  fieldIsEmpty(){
    if( !this.editForm.get(['login']).value.toString().isEmpty&&
      this.editForm.get(['login']).value.toString() != "" &&
    !this.editForm.get(['firstName']).value.toString().isEmpty &&
      this.editForm.get(['firstName']).value.toString()!=""&&
      !this.editForm.get(['lastName']).value.toString().isEmpty &&
      this.editForm.get(['lastName']).value.toString() !=""&&
      !this.editForm.get(['email']).value.toString().isEmpty&&
      this.editForm.get(['email']).value.toString() !=""
    )
      this.IsFieldsEmpty = false;
    else
      this.IsFieldsEmpty = true;

      // alert(this.IsFieldsEmpty)
  }
  internalCheckBox(): boolean {
    if (this.isInternal == true) {
      this.editForm.controls['login'].enable();
      this.editForm.controls['firstName'].enable();
      this.editForm.controls['lastName'].enable();
      this.editForm.controls['email'].enable();
      return this.isInternal = false;
      const searchControl = this.editForm.get('search');
      searchControl.setValidators([]);

    } else {
      this.editForm.controls['login'].disable();
      this.editForm.controls['firstName'].disable();
      this.editForm.controls['lastName'].disable();
      this.editForm.controls['email'].disable();
      return this.isInternal = true;
    }

  }

  private initClientsAndExistingRoles(res: IClient[]): void {
    this.clients = res;
    this.filtredClients = res;
    if (!this.isCreate) {
      this.user.clientRoles.forEach(clientRole => {
        const filteredClientRole = this.role.roleClient = this.clients.filter(client => client.id === clientRole.clientId)[0];
        if (filteredClientRole) {
          this.role.roleClient = this.clients.filter(client => client.id === clientRole.clientId)[0].name;
          this.role.clientId = clientRole.clientId;
          this.role.roleName = clientRole.roleName;

          this.roles.splice(this.roles.length, 0, this.role);
        }
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
          return;
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
      langKey: user.langKey,
      activated: user.activated,
      authorities: user.authorities
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.user.internal = this.isInternal;
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
    // if(this.translateAuthorities.length >0){
    //   for(let i =0; i<=this.translateAuthorities.length;i++){
    //     this.translateAuthorities.pop();
    //   }
    // }
    this.userService.getRoleAuthorities(this.role.roleName).subscribe(
      res => {
        this.text = res;

        this.text.forEach(element => {
          this.translateAuthorities.push(this.translateService.instant('userManagement.authorities.' + element) + "<br>");
        });


      },
      res => {
        console.log('An error has occurred when get role authorities');
      }
    );


    this.role = {};
    this.roleClient = null;
    this.roleName = null;
    this.editForm.patchValue({
      roleClient: '',
      roleName: ''
    });

    // for(let authority of this.text){
    //   this.translateAuthorities = this.translateAuthorities + this.translateService.instant('userManagement.authorities.'+authority)
    // }
    this.filterClients();
  }

  delete(id: number, index: number) {

    let lengthOfStack = this.translateAuthorities.length;
    for(let i =0; i <= lengthOfStack;i++){
      this.translateAuthorities.pop();
    }
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

  searchLdapUser() {
    console.log('login val: ' + this.editForm.controls['search'].value);
    this.userService.findLdapUser(this.editForm.controls['search'].value).subscribe(
      res => {
        console.log('Ldap user: ' + JSON.stringify(res));
        this.editForm.controls['login'].setValue(res.body.userName);
        this.editForm.controls['firstName'].setValue(res.body.firstName);
        this.editForm.controls['lastName'].setValue(res.body.lastName);
        this.editForm.controls['email'].setValue(res.body.userName + "@tamkeentech.sa");
        this.ldapDateEmpty =false;
      },
      res => {
        console.log('An error has occurred when get ldap user');
      }
    );
  }
}
