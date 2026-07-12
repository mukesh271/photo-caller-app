import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { Router } from '@angular/router';
import { DialerRoleService } from '../../services/dialer-role.service';
import { ContactsService } from '../../services/contacts.service';

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: './onboarding.page.html',
  styleUrls: ['./onboarding.page.scss'],
})
export class OnboardingPage implements OnInit {
  isDefaultDialer = false;
  hasContactsPermission = false;
  requesting = false;

  constructor(
    private dialerRole: DialerRoleService,
    private contacts: ContactsService,
    private router: Router
  ) {}

  async ngOnInit() {
    this.isDefaultDialer = await this.dialerRole.isDefaultDialer();
  }

  async requestDialerRole() {
    this.requesting = true;
    try {
      this.isDefaultDialer = await this.dialerRole.requestDefaultDialer();
    } finally {
      this.requesting = false;
    }
    if (this.isDefaultDialer) {
      await this.requestContactsPermission();
    }
  }

  async requestContactsPermission() {
    this.hasContactsPermission = await this.contacts.ensurePermission();
    if (this.isDefaultDialer && this.hasContactsPermission) {
      this.router.navigateByUrl('/contacts');
    }
  }
}
