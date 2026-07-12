import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { ContactsService, AppContact } from '../../services/contacts.service';
import { CallService } from '../../services/call.service';

@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [CommonModule, IonicModule],
  templateUrl: './contacts.page.html',
  styleUrls: ['./contacts.page.scss'],
})
export class ContactsPage implements OnInit {
  contacts: AppContact[] = [];
  loading = true;
  errorMessage: string | null = null;

  constructor(
    private contactsService: ContactsService,
    private callService: CallService
  ) {}

  async ngOnInit() {
    await this.loadContacts();
  }

  async loadContacts() {
    this.loading = true;
    this.errorMessage = null;
    try {
      this.contacts = await this.contactsService.getAllContactsPhotoFirst();
    } catch (err) {
      this.errorMessage = 'Could not load contacts. Please check permissions.';
    } finally {
      this.loading = false;
    }
  }

  call(contact: AppContact) {
    if (!contact.phoneNumber) return;
    this.callService.dial(contact.phoneNumber);
  }
}
