# 🗂️ M Kumar App — Project Tasks
_Last updated: 2025-11-15_

---

## 🔴 High Priority
- [x] Save item to database when product is added/updated  #high
- [x] Delete item immediately when user removes it  #high
- [x] Display total summary of the order at the bottom of the order sheet  #high
- [x] Generate PDF for order summary  #high
- [x] Improve PDF to match products table with order summary  #high
- [x] Share the PDF with application  #high
- [x] Search the customer  #high
- [x] PDF – Center align banner & address, add logo, and “Invoice” title  #high
- [x] Dashboard screen with summary of customers/orders added  #high
- [ ] GitHub sync (cloud implementation)  #high
- [ ] Add TableExtendedData column for future perspective
- [ ] Search and Sort Capability #high 
	- [x] Search by Invoice #high 
	- [ ] Search by has pending order #high 
	- [x] Sort by Created #high 
	- [x] Sort by Updated #high 

---

## 🟡 Medium Priority
- [x] Replace product selection dropdown with card-based UI  #medium
- [x] Remove “Save Order” and “Discard” buttons; start new order when “New Sale” is clicked  #medium
- [x] Add Material 3 expressive UI navigation animations  #medium
- [x] Align the Accordion UI height in Order bottom sheet  #medium
- [x] Update header of product accordion  #medium
- [x] PDF: Move Subtotal and Grand Total under Discount %  #medium
- [x] Standardize text field components (e.g. discount replaces 0 with actual text)  #medium
- [x] Add required fields in product forms and align multiple fields per row  #medium
- [x] Move ConfirmActionDialog to delete the customer on Search Screen
- [ ] Add Menu Items for Orders and Customers on Search Screen
- [x] Fix the Menu Items for Orders on Dashboard/Home Screen
- [ ] Add Filter on Customer Details Screen
- [ ] Store actual Product type when General Product is selected in content column of search_fts
- [ ] When last product is deleted, it should also delete all the payments? It should not, but need to confirm
- [x] Fix the total value on recents order screen
- [ ] Store everything in UTC format in database and then convert on UI based on local timezone
- [ ] Store emailid and dob of the customer

---

## 🔵 Low Priority
- [ ] Update Customer Details card on order sheet to look professional  #low
- [x] Align accordion card UI to match bottom sheet design  #low
- [x] Add confirmation dialog when deleting an item from order  #low
- [ ] Add toast/snackbar notification for add/update/delete actions  #low
- [ ] Code cleanup — remove redundant code  #low
- [x] Show app version in header (“M Kumar vX.Y”)  #low
- [x] Add setting to choose invoice date format  #low
- [x] Make phone number text field accept exactly 10 digits  #low
- [ ] Update Terms & Conditions with last-change date  #low
- [x] Change the name of the invoice file
- [ ] Make the row with dismiss and confirm buttons generic
- [x] when Orders is selected on Advanced Filter, force search by to Invoice
- [ ] Check how we are storing multiple orderids in search_fts for one customer with multiple orders

---

## 🕓 Pending / Future Ideas
- [ ] Consider dashboard analytics (optional)
- [ ] Add dark-mode adjustments for Material 3 expressive UI
- [ ] Automate PDF generation & GitHub cloud sync together
- [ ] Invoice Counter Control - through preference

---
