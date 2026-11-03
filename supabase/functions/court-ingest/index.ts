// Court provider adapter endpoint — intentionally fail-closed until an authorised
// court data provider, documented API contract and credentials are supplied.
// Do not scrape CAPTCHA-protected eCourts pages or accept browser-supplied results as verified.
import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
serve((_req) => new Response(JSON.stringify({
  ok: false,
  code: "COURT_PROVIDER_NOT_CONFIGURED",
  message: "No authorised court-data provider is configured. No court data was fetched or changed."
}), { status: 501, headers: { "content-type": "application/json" } }));
