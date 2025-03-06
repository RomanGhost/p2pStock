import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class RoleGuard implements CanActivate {
  constructor(private userService: UserService, private router: Router) {}

  async canActivate(route: ActivatedRouteSnapshot): Promise<boolean> {
    try {
      const allowedRoles: string[] = route.data['roles'] || [];
      const user = await firstValueFrom(this.userService.getUserProfile());
      console.log(user?.roleName.toLowerCase());
      if (user && allowedRoles.includes(user.roleName.toLowerCase())) {
        return true;
      }

      // Если роль не совпадает
      this.router.navigate(['/home']);
      return false;
    } catch (error) {
      // В случае ошибки (например, пользователь не авторизован)
      this.router.navigate(['/home']);
      return false;
    }
  }
}
