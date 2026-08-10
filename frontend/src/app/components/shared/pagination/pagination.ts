import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.html',
  styleUrl: './pagination.scss',
})
export class Pagination {
  readonly page = input.required<number>();
  readonly pageSize = input.required<number>();
  readonly totalElements = input.required<number>();

  readonly pageChange = output<number>();

  protected get totalPages(): number {
    return Math.ceil(this.totalElements() / this.pageSize());
  }

  protected get firstItem(): number {
    if (this.totalElements() === 0) {
      return 0;
    }

    return this.page() * this.pageSize() + 1;
  }

  protected get lastItem(): number {
    return Math.min((this.page() + 1) * this.pageSize(), this.totalElements());
  }

  protected previousPage(): void {
    if (this.page() > 0) {
      this.pageChange.emit(this.page() - 1);
    }
  }

  protected nextPage(): void {
    if (this.page() < this.totalPages - 1) {
      this.pageChange.emit(this.page() + 1);
    }
  }
}
