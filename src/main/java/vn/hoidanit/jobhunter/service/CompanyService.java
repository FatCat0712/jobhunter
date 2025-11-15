package vn.hoidanit.jobhunter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

import java.util.Optional;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(Company c) {
        return companyRepository.save(c);
    }

    public ResultPaginationDTO listAllCompanies(Specification<Company> spec, Pageable pageable) {
        Page<Company> page = companyRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);


        rs.setResult(page.getContent());

        return rs;
    }

    public Company fetchCompanyById(Long companyId) {
        Optional<Company> savedCompany =  companyRepository.findById(companyId);
        return savedCompany.orElse(null);
    }

    public Company updateCompany(Company c) {
        Company inDbCompany = fetchCompanyById(c.getId());
        if(inDbCompany != null) {
            inDbCompany.setName(c.getName());
            inDbCompany.setDescription(c.getDescription());
            inDbCompany.setAddress(c.getAddress());
            inDbCompany.setLogo(c.getLogo());

            companyRepository.save(inDbCompany);
        }
        return inDbCompany;
    }

    public void deleteCompany(Long companyId) {
        Company companyInDB = fetchCompanyById(companyId);

        if(companyInDB != null) {
            companyInDB.getUsers().clear();
            companyRepository.deleteById(companyId);
        }



    }
}
