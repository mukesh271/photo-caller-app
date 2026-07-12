import { Injectable } from '@angular/core';
import { CallNumber } from 'capacitor-call-number';
// npm install capacitor-call-number — thin wrapper around
// Intent.ACTION_CALL, works fine once CALL_PHONE permission is granted.
// Since this app is the default dialer, calls placed this way are routed
// straight through PhotoCallInCallService automatically.

@Injectable({ providedIn: 'root' })
export class CallService {
  async dial(phoneNumber: string): Promise<void> {
    await CallNumber.call({ number: phoneNumber, bypassAppChooser: true });
  }
}
