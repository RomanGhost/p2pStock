import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DealRequestsComponent } from './deal-requests.component';

describe('DealRequestsComponent', () => {
  let component: DealRequestsComponent;
  let fixture: ComponentFixture<DealRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealRequestsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DealRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
