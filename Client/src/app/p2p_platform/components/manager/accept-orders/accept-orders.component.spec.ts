import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AcceptOrdersComponent } from './accept-orders.component';

describe('AcceptOrdersComponent', () => {
  let component: AcceptOrdersComponent;
  let fixture: ComponentFixture<AcceptOrdersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AcceptOrdersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AcceptOrdersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
