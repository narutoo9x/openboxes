/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product

class ProductGroupController {

    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def productService 
	
    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productGroupInstanceList: ProductGroup.list(params), productGroupInstanceTotal: ProductGroup.count()]
    }

    def create = {
        def productGroupInstance = new ProductGroup()
        productGroupInstance.properties = params
        return [productGroupInstance: productGroupInstance]
    }

    def save = {
        def productGroupInstance = new ProductGroup(params)
		//productGroupInstance.products = productService.getProducts(params['product.id'])
		def products = productService.getProducts(params['product.id'])
		println "Products: " + products
		products.each { product ->
			productGroupInstance.addToProducts(product)
        }
		
		if (productGroupInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
            redirect(action: "edit", id: productGroupInstance.id)
        }
        else {
            render(view: "create", model: [productGroupInstance: productGroupInstance])
        }
    }

    def show = {
        def productGroupInstance = ProductGroup.get(params.id)
        if (!productGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
        else {
            [productGroupInstance: productGroupInstance]
        }
    }

    def edit = {
		log.info "Edit product group: " + params
		
        def productGroupInstance = ProductGroup.get(params.id)
        if (!productGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
        else {
			productGroupInstance.properties = params
			log.info "category: " + productGroupInstance?.category?.name
            return [productGroupInstance: productGroupInstance]
        }
    }

	def addProducts = { 
	
		def productGroupInstance = ProductGroup.get(params.id)
		if (productGroupInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (productGroupInstance.version > version) {
					
					productGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productGroup.label', default: 'ProductGroup')] as Object[], "Another user has updated this ProductGroup while you were editing")
					render(view: "edit", model: [productGroupInstance: productGroupInstance])
					return
				}
			}
			productGroupInstance.properties = params
			
			log.info("Products to add " + params['product.id'])
			
			log.info("Products before " + productGroupInstance.products)
			
			def products = productService.getProducts(params['product.id'])
			println "Products: " + products
			products.each { product ->
				productGroupInstance.addToProducts(product)
			}

			log.info("Products after " + productGroupInstance.products)
			
			if (!productGroupInstance.hasErrors() && productGroupInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
				redirect(action: "edit", id: productGroupInstance.id)
			}
			else {
				render(view: "edit", model: [productGroupInstance: productGroupInstance])
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
			redirect(action: "list")
		}
			
	}
	
    def update = {
		
		log.info "Update product group " + params 
		
		
        def productGroupInstance = ProductGroup.get(params.id)
        if (productGroupInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productGroupInstance.version > version) {
                    
                    productGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productGroup.label', default: 'ProductGroup')] as Object[], "Another user has updated this ProductGroup while you were editing")
                    render(view: "edit", model: [productGroupInstance: productGroupInstance])
                    return
                }
            }
			productGroupInstance.properties = params
			
			// The user changed the category, so we want to redisplay the form with no products
			if (params?.oldCategory?.id != productGroupInstance?.category?.id) { 
				productGroupInstance.products = []
				render(view: "edit", model: [productGroupInstance: productGroupInstance])
				return
			}
			
			//productGroupInstance.products = productService.getProducts(params['product.id'])
			
			def products = productService.getProducts(params['product.id'])
			println "Products: " + products
			products.each { product ->
				productGroupInstance.addToProducts(product)
			}
            if (!productGroupInstance.hasErrors() && productGroupInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
                redirect(action: "edit", id: productGroupInstance.id)
            }
            else {
                render(view: "edit", model: [productGroupInstance: productGroupInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productGroupInstance = ProductGroup.get(params.id)
        if (productGroupInstance) {
            try {
                productGroupInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
    }
	
	def addToProductGroup = { 
		def productGroupInstance = new ProductGroup()
		productGroupInstance.properties = params
		productGroupInstance.products = productService.getProducts(params['product.id'])
		
		def categories = productGroupInstance.products.collect { 
			it.category
		}
		
		categories = categories.unique();
	
		if (categories.size() > 1) { 
			//throw new Exception("Product group must contain products from a single category")
			productGroupInstance.errors.rejectValue("category", "Product group must contain products from a single category")			
			flash.message = "Please return to the <a href='javascript:history.go(-1)'>Inventory Browser</a> to choose products from a single category."
		}
		productGroupInstance.category = categories.get(0)
		

		render(view: "create", model: [productGroupInstance: productGroupInstance])
	}
	
}