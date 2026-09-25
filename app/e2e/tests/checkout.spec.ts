import { test, expect } from '@playwright/test';
import { addToCart, login } from './helpers';

test.describe('E2E-01 Kaufabschluss', () => {
  test('Anmelden, suchen, in den Warenkorb, Kasse, Bestellung bezahlt', async ({ page }) => {
    await page.goto('/');
    await login(page);

    await page.getByTestId('search-input').fill('Kabel');
    await page.getByTestId('search-submit').click();
    await expect(page.getByTestId('product-4')).toBeVisible();

    await addToCart(page, 4, 'CAB-6M', 3);
    await expect(page.getByTestId('cart-total')).toHaveText('29,94 €');

    await page.getByTestId('to-checkout').click();
    await page.getByTestId('shipping-PARCEL').check();
    await page.getByTestId('payment-CARD').check();
    await expect(page.getByTestId('checkout-total')).toHaveText('29,94 €');
    await page.getByTestId('place-order').click();

    await expect(page).toHaveURL(/\/orders\/\d+$/);
    await expect(page.getByTestId('order-status')).toHaveText('PAID');
    await expect(page.getByTestId('order-total')).toHaveText('29,94 €');
    await expect(page.getByTestId('payment-reference')).toContainText('pay-');
  });

  test('Ratenkauf bleibt offen, bis der Zahlungsdienstleister den Callback schickt', async ({ page, request }) => {
    await page.goto('/');
    await login(page);
    await addToCart(page, 1, 'GIT-STRAT-BK-R');

    await page.getByTestId('to-checkout').click();
    await page.getByTestId('payment-INSTALLMENTS').check();
    await page.getByTestId('place-order').click();

    await expect(page.getByTestId('order-status')).toHaveText('NEW');
    const reference = (await page.getByTestId('payment-reference').textContent())?.trim();
    expect(reference).toMatch(/^inst-/);

    // Der Test übernimmt die Rolle des Zahlungsdienstleisters und schickt den Callback.
    const token = process.env.PAYMENT_CALLBACK_TOKEN;
    if (!token) throw new Error('PAYMENT_CALLBACK_TOKEN muss gesetzt sein');
    const callback = () =>
      request.post('/api/payments/callback', {
        headers: { 'X-Callback-Token': token },
        data: { reference, status: 'AUTHORIZED' },
      });
    expect((await callback()).ok()).toBeTruthy();
    // Ein zweiter Callback derselben Zahlung darf nichts ändern.
    expect(await (await callback()).text()).toBe('ALREADY_PROCESSED');

    await page.reload();
    await expect(page.getByTestId('order-status')).toHaveText('PAID');
  });
});
