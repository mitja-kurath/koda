import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';

@Component({
  selector: 'app-home',
  imports: [RouterLink, HlmButtonImports, HlmTypographyImports, HlmCardImports],
  templateUrl: './home.html',
})
export class HomeComponent {}
