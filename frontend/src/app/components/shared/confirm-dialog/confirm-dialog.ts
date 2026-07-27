import { Component, computed, inject, signal } from '@angular/core';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { FormsModule } from '@angular/forms';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText: string;
}

@Component({
  selector: 'app-confirm-delete-network-dialog',
  imports: [FormsModule],
  templateUrl: './confirm-dialog.html',
})
export class ConfirmDialog {
  protected readonly data = inject<ConfirmDialogData>(DIALOG_DATA);
  private readonly dialogRef = inject(DialogRef<boolean>);

  protected readonly confirmation = signal('');
  protected readonly canConfirm = computed(
    () => this.confirmation().trim() === this.data.confirmText,
  );

  protected cancel() {
    this.dialogRef.close(false);
  }

  protected confirm() {
    if (!this.canConfirm()) return;
    this.dialogRef.close(true);
  }
}
