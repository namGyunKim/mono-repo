import { test, expect } from '@playwright/test';

const USER_API_URL =
  process.env['USER_API_URL'] ?? 'http://localhost:8081';

test.describe('Backend Health Check', () => {
  test('user-api /api/health returns 200', async ({ request }) => {
    const response = await request.get(`${USER_API_URL}/api/health`);
    expect(response.ok()).toBeTruthy();
  });
});
