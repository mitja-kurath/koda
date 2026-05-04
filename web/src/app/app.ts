import { Component, signal } from '@angular/core';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';

@Component({
  selector: 'app-root',
  imports: [HlmButtonImports, HlmTypographyImports, HlmCardImports],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Koda');
  protected readonly version = signal('0.0.1');
}
