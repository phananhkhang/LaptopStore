package org.akira.laptopstore.cart;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CartArchitectureTest {
    @Test
    void cartMustNotDependOnCatalogWebOrServiceLayers() {
        noClasses()
                .that().resideInAPackage("..cart..")
                .should().dependOnClassesThat().resideInAnyPackage("..catalog.controller..", "..catalog.service..")
                .check(new ClassFileImporter().importPackages("org.akira.laptopstore.cart"));
    }
}
