package com.autoadda.apis.other.config;

import com.autoadda.apis.other.entity.os.OsAvailableService;
import com.autoadda.apis.other.repository.os.OsAvailableServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OsAvailableServiceSeeder {

    @Bean
    CommandLineRunner initOsAvailableServices(OsAvailableServiceRepository repository) {
        return args -> {
            List<OsAvailableService> defaultServices = List.of(
                    new OsAvailableService(1, "Tyre & Balancing", "", "https://img.freepik.com/premium-photo/png-three-automobile-tires-mag-wheel-vehicle-spoke-car_53876-752756.jpg?w=2000"),
                    new OsAvailableService(2, "Suspension", "", "https://images.squarespace-cdn.com/content/v1/57a3f72d4402439bb069fed6/1525196667874-HEXAJBHJ48GV5EHCR2GK/car-suspension.png"),
                    new OsAvailableService(3, "Battery", "","https://cdn3d.iconscout.com/3d/premium/thumb/car-battery-3d-illustration-download-in-png-blend-fbx-gltf-file-formats--charging-accumulator-energy-electricity-pack-appliances-illustrations-3535935.png?f=webp"),
                    new OsAvailableService(4, "Recovery", "","https://resources.servicemycar.com/upimages/service-content/Car-recovery-Dubai-20221222040011.png"),
                    new OsAvailableService(5,"Insurance","","https://cdn3d.iconscout.com/3d/premium/thumb/car-insurance-3d-icon-download-in-png-blend-fbx-gltf-file-formats--automobile-vehicle-claim-pack-services-icons-7767112.png" ),
                    new OsAvailableService(6, "Car Inspection", "", "https://cdn3d.iconscout.com/3d/premium/thumb/car-insurance-3d-icon-download-in-png-blend-fbx-gltf-file-formats--auto-coverage-planning-vehicle-protection-security-financial-pack-business-icons-7994011.png?f=webp"),
                    new OsAvailableService(7, "Quality Control", "","https://cdn3d.iconscout.com/3d/premium/thumb/quality-assurance-3d-icon-download-in-png-blend-fbx-gltf-file-formats--check-report-management-digital-skills-pack-future-technology-icons-5873653.png?f=webp"),
                    new OsAvailableService(8,"Engineering Work","","https://cdn3d.iconscout.com/3d/premium/thumb/automotive-parts-3d-icon-download-in-png-blend-fbx-gltf-file-formats--futuristic-line-engineering-factory-pack-industry-icons-10411102.png?f=webp"),
                    new OsAvailableService(9,"Hydraulic Hose","","https://hydrohose.ae/wp-content/uploads/2023/11/Untitled-2380-x-1329-px-1024x572.png"),
                    new OsAvailableService(10,"Part Repairing","","https://png.pngtree.com/png-vector/20240322/ourmid/pngtree-car-maintenance-and-repairing-center-services-png-image_12186512.png"),
                    new OsAvailableService(11,"Accessories","","https://png.pngtree.com/png-clipart/20230814/original/pngtree-car-service-illustration-repair-auto-picture-image_7921951.png"),
                    new OsAvailableService(12,"Upholstery","","https://cdn3d.iconscout.com/3d/premium/thumb/clean-upholstery-3d-icon-download-in-png-blend-fbx-gltf-file-formats--car-seat-safety-wash-pack-business-icons-10402934.png"),
                    new OsAvailableService(13,"Car Wash","","https://cdn3d.iconscout.com/3d/premium/thumb/car-wash-machine-3d-icon-download-in-png-blend-fbx-gltf-file-formats--cleaning-services-detailing-pack-vehicle-icons-8363553.png"),

                    new OsAvailableService(14,"Car Detailing","","https://img.favpng.com/4/10/17/car-wash-vector-graphics-clip-art-cleaning-png-favpng-K1usSNNsUzKtA80RaERLrTcgS.jpg"),
                    new OsAvailableService(15,"Modification","", "https://png.pngtree.com/png-vector/20241120/ourmid/pngtree-colorful-modified-car-with-artistic-graffiti-design-png-image_14499086.png"),
                    new OsAvailableService(16,"Glass Services","","https://vipautoglasstx.com/wp-content/uploads/2020/06/Auto-Glass-Repair-Katy-Tomball-Texas-1002x1024.png.webp"),
                    new OsAvailableService(17,"Oil Change","","https://cdn3d.iconscout.com/3d/premium/thumb/oil-change-3d-icon-download-in-png-blend-fbx-gltf-file-formats--barrel-container-can-car-service-pack-services-icons-10634585.png?f=webp"),

                    new OsAvailableService(18,"Fuel Services","","https://cdn3d.iconscout.com/3d/premium/thumb/petrol-station-3d-icon-download-in-png-blend-fbx-gltf-file-formats--gas-fuel-pump-mechanical-car-repair-tool-pack-service-icons-8317514.png?f=webp"),
                    new OsAvailableService(19,"EV Services","","https://corkautoservices.com/wp-content/uploads/2019/04/Electric-Car-Servicing-Cork-Auto-Services.png"),
                    new OsAvailableService(20,"Tools","","https://cdn3d.iconscout.com/3d/premium/thumb/tool-box-3d-icon-download-in-png-blend-fbx-gltf-file-formats--kit-tools-repair-labor-day-pack-industry-icons-6740546.png"),
                    new OsAvailableService(21,"Delivery","","https://cdn3d.iconscout.com/3d/premium/thumb/tool-box-3d-icon-download-in-png-blend-fbx-gltf-file-formats--kit-tools-repair-labor-day-pack-industry-icons-6740546.png")



            );

            for (OsAvailableService service : defaultServices) {
                repository.findByName(service.getName())
                        .orElseGet(() -> repository.save(service));
            }
        };
    }
}
