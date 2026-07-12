import { Injectable } from '@angular/core';
import { Contacts, PermissionStatus } from '@capacitor-community/contacts';

export interface AppContact {
  contactId: string;
  displayName: string;
  photoDataUrl: string | null; // base64 image, or null -> use default avatar
  phoneNumber: string | null;
}

@Injectable({ providedIn: 'root' })
export class ContactsService {
  async ensurePermission(): Promise<boolean> {
    const status: PermissionStatus = await Contacts.checkPermissions();
    if (status.contacts === 'granted') return true;

    const requested = await Contacts.requestPermissions();
    return requested.contacts === 'granted';
  }

  async getAllContacts(): Promise<AppContact[]> {
    const hasPermission = await this.ensurePermission();
    if (!hasPermission) {
      throw new Error('Contacts permission was not granted');
    }

    const result = await Contacts.getContacts({
      projection: {
        name: true,
        phones: true,
        image: true,
      },
    });

    return result.contacts
      .filter((c) => c.phones && c.phones.length > 0)
      .map((c) => ({
        contactId: c.contactId,
        displayName: c.name?.display ?? 'Unknown',
        photoDataUrl: c.image?.base64String
          ? `data:image/jpeg;base64,${c.image.base64String}`
          : null,
        phoneNumber: c.phones?.[0]?.number ?? null,
      }));
  }

  /**
   * Sorted so contacts WITH a photo come first — for a photo-first UI,
   * contacts without an assigned photo are the ones a caregiver still
   * needs to fix, so surfacing them separately (or fixing them up front
   * during onboarding) matters more than alphabetical order.
   */
  async getAllContactsPhotoFirst(): Promise<AppContact[]> {
    const contacts = await this.getAllContacts();
    return [...contacts].sort((a, b) => {
      if (a.photoDataUrl && !b.photoDataUrl) return -1;
      if (!a.photoDataUrl && b.photoDataUrl) return 1;
      return a.displayName.localeCompare(b.displayName);
    });
  }
}
