import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TbsLandingComponent} from './tbs-landing.component';

describe('SwiftLandingComponent', () => {
  let component: TbsLandingComponent;
  let fixture: ComponentFixture<TbsLandingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TbsLandingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TbsLandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
