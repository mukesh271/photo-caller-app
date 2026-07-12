import { Injectable } from '@angular/core';
import { registerPlugin } from '@capacitor/core';

export interface DialerRolePlugin {
  isDefaultDialer(): Promise<{ isDefault: boolean }>;
  requestDefaultDialer(): Promise<{ granted: boolean }>;
}

// Matches @CapacitorPlugin(name = "DialerRole") on the native side
const DialerRole = registerPlugin<DialerRolePlugin>('DialerRole');

@Injectable({ providedIn: 'root' })
export class DialerRoleService {
  async isDefaultDialer(): Promise<boolean> {
    const { isDefault } = await DialerRole.isDefaultDialer();
    return isDefault;
  }

  /**
   * Triggers the system "set as default phone app" prompt.
   * Resolves once the user responds (granted true/false).
   */
  async requestDefaultDialer(): Promise<boolean> {
    const { granted } = await DialerRole.requestDefaultDialer();
    return granted;
  }
}
