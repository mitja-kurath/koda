import { Component, input, computed } from '@angular/core';
import { HlmAccordionImports } from '@spartan-ng/helm/accordion';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';

interface OpenApiInfo { title: string; version: string; description?: string; }
interface OpenApiParameter { name: string; in: string; required?: boolean; description?: string; schema?: { type?: string; format?: string; }; }
interface OpenApiOperation { summary?: string; description?: string; tags?: string[]; parameters?: OpenApiParameter[]; requestBody?: { description?: string; required?: boolean; content?: Record<string, unknown>; }; responses?: Record<string, { description?: string; }>; }
interface OpenApiPaths { [path: string]: { [method: string]: OpenApiOperation }; }
interface ParsedSpec { info: OpenApiInfo; servers?: { url: string; description?: string; }[]; tagGroups: { tag: string; endpoints: { path: string; method: string; op: OpenApiOperation; }[]; }[]; }

const HTTP_METHODS = ['get', 'post', 'put', 'patch', 'delete', 'options', 'head'];

@Component({
  selector: 'app-spec-viewer',
  imports: [HlmAccordionImports, HlmBadgeImports, HlmTypographyImports],
  templateUrl: './spec-viewer.html',
})
export class SpecViewerComponent {
  readonly spec = input.required<string>();

  readonly parsed = computed<ParsedSpec | null>(() => {
    try {
      return this.parse(this.spec());
    } catch {
      return null;
    }
  });

  methodClass(method: string): string {
    const map: Record<string, string> = {
      get:    'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      post:   'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      put:    'bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200',
      patch:  'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
      delete: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    };
    return map[method.toLowerCase()] ?? 'bg-muted text-muted-foreground';
  }

  responsesOf(op: OpenApiOperation): { code: string; description: string }[] {
    if (!op.responses) return [];
    return Object.entries(op.responses).map(([code, val]) => ({
      code,
      description: (val as { description?: string }).description ?? '',
    }));
  }

  responseClass(status: string): string {
    const code = parseInt(status, 10);
    if (code < 300) return 'text-green-600';
    if (code < 400) return 'text-blue-600';
    if (code < 500) return 'text-amber-600';
    return 'text-red-600';
  }

  private parse(raw: string): ParsedSpec {
    const obj = JSON.parse(raw) as { info: OpenApiInfo; servers?: { url: string; description?: string; }[]; paths?: OpenApiPaths; tags?: { name: string; }[]; };
    const paths: OpenApiPaths = obj.paths ?? {};
    const allTags = new Set<string>((obj.tags ?? []).map((t) => t.name));

    type Endpoint = { path: string; method: string; op: OpenApiOperation; };
    const byTag = new Map<string, Endpoint[]>();
    const untagged: Endpoint[] = [];

    for (const [path, pathItem] of Object.entries(paths)) {
      for (const method of HTTP_METHODS) {
        const op = (pathItem as Record<string, OpenApiOperation>)[method];
        if (!op) continue;
        const tags = op.tags?.length ? op.tags : null;
        if (!tags) { untagged.push({ path, method, op }); continue; }
        for (const tag of tags) {
          allTags.add(tag);
          if (!byTag.has(tag)) byTag.set(tag, []);
          byTag.get(tag)!.push({ path, method, op });
        }
      }
    }

    const tagGroups = Array.from(byTag.entries()).map(([tag, endpoints]) => ({ tag, endpoints }));
    if (untagged.length) tagGroups.push({ tag: 'Other', endpoints: untagged });

    return { info: obj.info, servers: obj.servers, tagGroups };
  }
}
