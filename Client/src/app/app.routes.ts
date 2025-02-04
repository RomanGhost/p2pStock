import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { RegisterComponent } from './components/register/register.component';
import { AuthGuard } from './auth.guard';
import { AccountComponent } from './components/account/account.component';
import { LoginComponent } from './components/login/login.component';
import { AddOrderComponent } from './components/add-order/add-order.component';
import { AllOrdersComponent } from './components/all-orders/all-orders.component';
import { AcceptOrdersComponent } from './components/manager/accept-orders/accept-orders.component';
import { RoleGuard } from './role.guard';
import { TaskComponent } from './components/manager/task/task.component';
import { UserControllComponent } from './components/admin/user-controll/user-controll.component';

export const routes: Routes = [
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'home', component: HomeComponent },
    { path: 'account', component: AccountComponent, canActivate: [AuthGuard] },
    { path: 'orders/add', component: AddOrderComponent, canActivate: [AuthGuard] },
    { path: 'orders', component: AllOrdersComponent , canActivate: [AuthGuard] },
    { path: "manager/orders", component: AcceptOrdersComponent , canActivate: [RoleGuard], data: { roles: ['manager'] } },
    { path: "manager/tasks", component: TaskComponent , canActivate: [RoleGuard], data: { roles: ['manager'] } },
    { path: "admin/users", component: UserControllComponent , canActivate: [RoleGuard], data: { roles: ['admin'] } },
  ];

  