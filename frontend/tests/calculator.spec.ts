import {test,expect} from '@playwright/test';

test('Java calculation, step adjustment, reference rules, validation and higher derivatives',async({page})=>{
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));
 await page.goto('/');await expect(page.locator('.answer')).toHaveText('14.01');await expect(page.locator('.plot .main-svg').first()).toBeVisible();
 await expect(page.locator('.step')).toHaveCount(9);
 await page.getByLabel('Step size h',{exact:true}).fill('0.01');await expect(page.locator('.answer')).toHaveText('14.0001');
 await page.getByLabel('Function f(x)',{exact:true}).fill('x^2');await expect(page.getByLabel('Reference derivative value optional')).toHaveValue('');await page.getByRole('button',{name:'Calculate derivative'}).click();await expect(page.locator('.answer')).toHaveText('4');
 await page.getByLabel('Function f(x)',{exact:true}).fill('sqrt(x)');await page.getByLabel('Evaluation point x').fill('-2');await page.getByRole('button',{name:'Calculate derivative'}).click();await expect(page.getByRole('alert')).toContainText('undefined');
 await page.locator('.example-chips button').nth(3).click();await page.getByRole('button',{name:'Calculate derivative'}).click();await expect(page.locator('.answer')).toHaveText('48.02');
 await page.getByLabel('Derivative order').selectOption('4');await page.locator('#method-forward').click();await expect(page.getByLabel('Accuracy')).toHaveValue('O_H');await page.getByRole('button',{name:'Calculate derivative'}).click();await expect(page.locator('.answer')).toHaveText(/24/);
 expect(errors).toEqual([]);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBeTruthy();
});
test('comparison, learning, local history and theme',async({page})=>{
 await page.goto('/');await expect(page.locator('.answer')).toHaveText('14.01');await page.getByRole('button',{name:'Calculate derivative'}).click();await expect(page.locator('.answer')).toHaveText('14.01');
 await page.locator('nav a[href="/history"]').click();await expect(page.locator('.history-list article')).toHaveCount(1);await page.getByRole('link',{name:'Restore',exact:true}).click();await expect(page.getByLabel('Function f(x)',{exact:true})).toHaveValue('x^3 + 2*x');
 await page.goto('/compare');await expect(page.locator('tbody tr')).toHaveCount(6);await expect(page.locator('tr.best')).toHaveCount(1);await page.getByRole('button',{name:'Toggle dark mode'}).click();await expect(page.locator('html')).toHaveAttribute('data-theme','dark');
 await page.locator('nav a[href="/learn"]').click();await expect(page.locator('.lesson')).toHaveCount(8);await page.reload();await expect(page.locator('html')).toHaveAttribute('data-theme','dark');
 await page.locator('nav a[href="/history"]').click();await page.getByRole('button',{name:'Clear all history'}).click();await expect(page.locator('.history-list article')).toHaveCount(0);
});
