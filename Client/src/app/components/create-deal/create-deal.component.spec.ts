import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateDealComponent } from './create-deal.component';

describe('CreateDealComponent', () => {
  let component: CreateDealComponent;
  let fixture: ComponentFixture<CreateDealComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateDealComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateDealComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
