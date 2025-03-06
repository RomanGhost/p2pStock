import { Routes } from '@angular/router';
import { HomeComponent } from './p2p_platform/components/home/home.component';
import { RegisterComponent } from './p2p_platform/components/register/register.component';
import { AuthGuard } from './p2p_platform/guards/auth.guard';
import { AccountComponent } from './p2p_platform/components/account/account.component';
import { LoginComponent } from './p2p_platform/components/login/login.component';
import { AddOrderComponent } from './p2p_platform/components/add-order/add-order.component';
import { AllOrdersComponent } from './p2p_platform/components/all-orders/all-orders.component';
import { AcceptOrdersComponent } from './p2p_platform/components/manager/accept-orders/accept-orders.component';
import { RoleGuard } from './p2p_platform/guards/role.guard';
import { TaskComponent } from './p2p_platform/components/manager/task/task.component';
import { UserControllComponent } from './p2p_platform/components/admin/user-controll/user-controll.component';
import { ChatComponent } from './chat/component/chat/chat.component';

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
    { path: "chat", component: ChatComponent , canActivate: [AuthGuard] },
  ];

  