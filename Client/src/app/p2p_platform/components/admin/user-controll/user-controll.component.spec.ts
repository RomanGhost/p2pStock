import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserControllComponent } from './user-controll.component';

describe('UserControllComponent', () => {
  let component: UserControllComponent;
  let fixture: ComponentFixture<UserControllComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserControllComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserControllComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
