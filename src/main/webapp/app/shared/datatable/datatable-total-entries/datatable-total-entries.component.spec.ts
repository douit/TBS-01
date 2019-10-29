import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatatableTotalEntriesComponent } from './datatable-total-entries.component';

describe('DatatableTotalEntriesComponent', () => {
  let component: DatatableTotalEntriesComponent;
  let fixture: ComponentFixture<DatatableTotalEntriesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatatableTotalEntriesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatatableTotalEntriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
