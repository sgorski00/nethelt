import { Component, computed, inject, signal } from '@angular/core';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { FormsModule } from '@angular/forms';

export interface ConfirmDeleteNetworkDialogData {
  networkName: string;
}

@Component({
  selector: 'app-confirm-delete-network-dialog',
  imports: [FormsModule],
  templateUrl: './confirm-delete-network-dialog.html',
  styleUrl: './confirm-delete-network-dialog.scss',
})
export class ConfirmDeleteNetworkDialog {
  protected readonly data = inject<ConfirmDeleteNetworkDialogData>(DIALOG_DATA);
  private readonly dialogRef = inject(DialogRef<boolean>);

  protected readonly confirmation = signal('');
  protected readonly canDelete = computed(
    () => this.confirmation().trim() === this.data.networkName,
  );

  protected cancel() {
    this.dialogRef.close(false);
  }

  protected delete() {
    if (!this.canDelete()) return;
    this.dialogRef.close(true);
  }
}
