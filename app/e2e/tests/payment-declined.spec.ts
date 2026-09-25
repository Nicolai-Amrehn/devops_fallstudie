import { test, expect } from '@playwright/test';
import { addToCart, login } from './helpers';

test.describe('E2E-04 Zahlung abgelehnt und Sperrgut', () => {
  test('Konzertflügel: nur Spedition wählbar, Kartenzahlung abgelehnt, keine Bestellung', async ({ page }) => {
    await page.goto('/');
    await login(page);
    await addToCart(page, 2, 'PNO-F188-BK');

    await page.getByTestId('to-checkout').click();
    await expect(page.getByTestId('bulky-hint')).toBeVisible();
    await expect(page.getByTestId('shipping-PARCEL')).toHaveCount(0);
    await expect(page.getByTestId('shipping-FREIGHT')).toBeChecked();

    await page.getByTestId('payment-CARD').check();
    await page.getByTestId('place-order').click();

    await expect(page.getByTestId('checkout-error')).toContainText('abgelehnt');
    await expect(page).toHaveURL(/\/checkout$/);
    await page.goto('/orders');
    await expect(page.getByText('Konzertflügel')).toHaveCount(0);
  });

  test('Ratenkauf über dem Limit wird mit Hinweis abgelehnt', async ({ page }) => {
    await page.goto('/');
    await login(page);
    await addToCart(page, 2, 'PNO-F188-BK');
    await page.getByTestId('to-checkout').click();
    await page.getByTestId('payment-INSTALLMENTS').check();
    await page.getByTestId('place-order').click();

    await expect(page.getByTestId('checkout-error')).toContainText('Ratenkauf');
  });
});
