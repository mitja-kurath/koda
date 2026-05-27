import { Component, input, computed, inject, SecurityContext } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { marked } from 'marked';

@Component({
  selector: 'app-markdown-view',
  template: `<div class="prose prose-sm dark:prose-invert max-w-none" [innerHTML]="html()"></div>`,
})
export class MarkdownViewComponent {
  readonly content = input.required<string>();

  private readonly sanitizer = inject(DomSanitizer);

  readonly html = computed(() => {
    const raw = marked.parse(this.content()) as string;
    return this.sanitizer.sanitize(SecurityContext.HTML, raw) ?? '';
  });
}
