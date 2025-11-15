package vn.hoidanit.jobhunter.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.CompanyService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {
    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/companies")
    public ResponseEntity<Company> createNewCompany(@Valid @RequestBody Company newCompany) {
        Company createdCompany = companyService.createCompany(newCompany);
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }

    @ApiMessage("fetch company by id")
    @GetMapping("/companies/{id}")
    public ResponseEntity<Company> fetchCompanyById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(companyService.fetchCompanyById(id));
    }

    @GetMapping("/companies")
    @ApiMessage("Fetch companies")
    public ResponseEntity<ResultPaginationDTO> listCompany(
            @Filter Specification<Company> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(companyService.listAllCompanies(spec, pageable));
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> updateCompany(@Valid @RequestBody Company newCompany) {
        Company createdCompany = companyService.updateCompany(newCompany);
        return new ResponseEntity<>(createdCompany, HttpStatus.OK);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable(name = "id") Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
