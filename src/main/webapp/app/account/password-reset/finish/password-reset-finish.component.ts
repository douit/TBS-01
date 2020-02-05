import {Component, OnInit, AfterViewInit, Renderer, ElementRef, OnDestroy} from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { PasswordResetFinishService } from './password-reset-finish.service';
import {JhiLanguageService} from 'ng-jhipster';
declare var $: any;

@Component({
  selector: 'app-password-reset-finish',
  templateUrl: './password-reset-finish.component.html'
})
export class PasswordResetFinishComponent implements OnInit, OnDestroy, AfterViewInit {
  test: Date = new Date();
  doNotMatch: string;
  error: string;
  keyMissing: boolean;
  success: string;
  modalRef: NgbModalRef;
  key: string;

  // UI
  private toggleButton: any;

  passwordForm = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]]
  });

  constructor(
    private passwordResetFinishService: PasswordResetFinishService,
    private route: ActivatedRoute,
    private elementRef: ElementRef,
    private renderer: Renderer,
    private fb: FormBuilder,
    private router: Router,
    private element: ElementRef,
    private languageService: JhiLanguageService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.key = params['key'];
    });
    this.keyMissing = !this.key;


    // UI
    const navbar: HTMLElement = this.element.nativeElement;
    this.toggleButton = navbar.getElementsByClassName('navbar-toggle')[0];
    const body = document.getElementsByTagName('body')[0];
    body.classList.add('login-page');
    body.classList.add('off-canvas-sidebar');
    const card = document.getElementsByClassName('card')[0];
    //setTimeout(function() {
      // after 1000 ms we add the class animated to the login/register card
      //that.card.classList.remove('card-hidden');
    //}, 700);

  }

  ngAfterViewInit() {
    if (this.elementRef.nativeElement.querySelector('#password') != null) {
      this.renderer.invokeElementMethod(this.elementRef.nativeElement.querySelector('#password'), 'focus', []);
    }
  }

  finishReset() {
    this.doNotMatch = null;
    this.error = null;
    const password = this.passwordForm.get(['newPassword']).value;
    const confirmPassword = this.passwordForm.get(['confirmPassword']).value;
    if (password !== confirmPassword) {
      this.doNotMatch = 'ERROR';
    } else {
      this.passwordResetFinishService.save({ key: this.key, newPassword: password }).subscribe(
        () => {
          this.success = 'OK';
        },
        () => {
          this.success = null;
          this.error = 'ERROR';
        }
      );
    }
  }

  ngOnDestroy() {
    const body = document.getElementsByTagName('body')[0];
    body.classList.remove('login-page');
    body.classList.remove('off-canvas-sidebar');
  }

  login() {
    this.router.navigate(['login']);
  }
}
