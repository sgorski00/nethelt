import { Component, inject, OnInit, signal } from '@angular/core';
import { NotificationService } from '../../../services/notification-service';
import { NotificationPreferencesResponse } from '../../../models/notifications/notification-preferences-response';
import { DialogRef } from '@angular/cdk/dialog';
import {
  NotificationChannel,
  NotificationChannelLabels,
} from '../../../models/notifications/notification-channels';

@Component({
  selector: 'app-notification-preferences',
  imports: [],
  templateUrl: './notification-preferences-dialog.html',
  styleUrl: './notification-preferences-dialog.scss',
})
export class NotificationPreferencesDialog implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly dialogRef = inject(DialogRef<NotificationPreferencesResponse>, {
    optional: true,
  });
  protected readonly NotificationChannelLabels = NotificationChannelLabels;
  public availableChannels = Object.values(NotificationChannel);
  public readonly errorMessage = signal('');
  public readonly successMessage = signal('');
  public readonly preferences = signal<NotificationPreferencesResponse | null>(null);

  ngOnInit(): void {
    this.reloadPreferences();
  }

  closeDialog() {
    this.dialogRef?.close();
  }

  containsChannel(channel: NotificationChannel): boolean {
    return this.preferences()?.enabledChannels.includes(channel) ?? false;
  }

  onChannelChange(channel: NotificationChannel, event: Event): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      this.notificationService.enableChannel(channel).subscribe({
        next: (preferences) => {
          this.preferences.set(preferences);
          this.successMessage.set(
            `Successfully enabled ${NotificationChannelLabels[channel]} notifications.`,
          );
        },
        error: (err) => this.setError(err.error.detail),
      });
    } else {
      this.notificationService.disableChannel(channel).subscribe({
        next: (preferences) => {
          this.preferences.set(preferences);
          this.successMessage.set(
            `Successfully disabled ${NotificationChannelLabels[channel]} notifications.`,
          );
        },
        error: (err) => this.setError(err.error.detail),
      });
    }
  }

  private reloadPreferences() {
    this.notificationService.getPreferences().subscribe((res) => this.preferences.set(res));
  }

  private setError(message: string | null) {
    this.errorMessage.set(message || 'An error occurred while updating preferences.');
  }
}
