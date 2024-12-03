import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet, RouterLink } from '@angular/router';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { FooterComponent } from './components/footer/footer.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: true,
  imports: [RouterOutlet, CommonModule, SidebarComponent, FooterComponent]
})
export class AppComponent {
  title = 'packages-client';
  showNavbar = true;

  constructor(private router: Router) {
    // this.router.events.subscribe((event) => {
    //   if (event instanceof NavigationEnd) {
    //     console.log('Navigated to:', event.urlAfterRedirects); // Лог текущего маршрута
    //     const hiddenNavbarRoutes = ['/register', '/login'];
    //     this.showNavbar = !hiddenNavbarRoutes.includes(event.urlAfterRedirects);
    //     console.log('Show Navbar:', this.showNavbar); // Лог состояния навигации
    //   }
    // });    
  }
}
