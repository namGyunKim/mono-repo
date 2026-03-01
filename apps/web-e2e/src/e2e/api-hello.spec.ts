import { test, expect } from '@playwright/test';

test.describe('Next.js API Route', () => {
  test('GET /api/hello returns greeting', async ({ request }) => {
    const response = await request.get('/api/hello');
    expect(response.ok()).toBeTruthy();
    expect(await response.text()).toBe('Hello, from API!');
  });
});
