/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */

/*
 * MiscType.java
 *
 * Created on April 2, 2002, 12:15 PM
 */

package megamek.common;

import java.util.Enumeration;

/**
 *
 * @author  Ben
 * @version
 */
public class MiscType extends EquipmentType {
    // equipment flags (okay, like every type of equipment has its own flag)
    // TODO: l2 equipment flags
    public static final int     F_HEAT_SINK         = 0x0001;
    public static final int     F_DOUBLE_HEAT_SINK  = 0x0002;
    public static final int     F_JUMP_JET          = 0x0004;
    public static final int     F_CLUB              = 0x0008;
    public static final int     F_HATCHET           = 0x0010;
    public static final int     F_TREE_CLUB         = 0x0020;
    public static final int     F_CASE              = 0x0040;
    public static final int     F_MASC              = 0x0080;
    public static final int     F_TSM               = 0x0100;
    public static final int     F_C3M               = 0x0200;
    public static final int     F_C3S               = 0x0400;
    public static final int     F_C3I               = 0x0800;
    public static final int     F_ARTEMIS           = 0x1000;
    public static final int     F_ECM               = 0x2000;
    public static final int     F_TARGCOMP          = 0x4000;
    public static final int     F_OTHER             = 0x8000;
    
    /** Creates new MiscType */
    public MiscType() {
        ;
    }
    
    
    public float getTonnage(Entity entity) {
        if (tonnage != TONNAGE_VARIABLE) {
            return tonnage;
        }
        // check for known formulas
        if (hasFlag(F_JUMP_JET)) {
            if (entity.getWeight() >= 55.0) {
                return 0.5f;
            } else if (entity.getWeight() >= 85.0) {
                return 1.0f;
            } else {
                return 2.0f;
            }
        } else if (hasFlag(F_HATCHET)) {
            return (float)Math.ceil(entity.getWeight() / 15.0);
        } else if (hasFlag(F_MASC)) {
            if (entity.getTechLevel() == TechConstants.T_CLAN_LEVEL_2) {
                return (float)Math.round(entity.getWeight() / 25.0f);
            }
            else {
                return (float)Math.round(entity.getWeight() / 20.0f);
            }
        } else if (hasFlag(F_TARGCOMP)) {
            // based on tonnage of direct_fire weaponry
            double fTons = 0.0;
            for (Enumeration e = entity.getWeapons(); e.hasMoreElements(); ) {
                Mounted m = (Mounted)e.nextElement();
                WeaponType wt = (WeaponType)m.getType();
                if (wt.hasFlag(WeaponType.F_DIRECT_FIRE)) {
                    fTons += wt.getTonnage(entity);
                }
            }
            if (entity.getTechLevel() == TechConstants.T_CLAN_LEVEL_2) {
                return (float)Math.ceil(fTons / 5.0f);
            }
            else {
                return (float)Math.ceil(fTons / 4.0f);
            }
        }
        
        // okay, I'm out of ideas
        return 1.0f;
    }
    
    public int getCriticals(Entity entity) {
        if (criticals != CRITICALS_VARIABLE) {
            return criticals;
        }
        // check for known formulas
        if (hasFlag(F_HATCHET)) {
            return (int)Math.ceil(entity.getWeight() / 15.0);
        } else if (hasFlag(F_DOUBLE_HEAT_SINK) && entity.getTechLevel() != TechConstants.T_CLAN_LEVEL_2) {
            return 3;
		} else if (hasFlag(F_DOUBLE_HEAT_SINK) && entity.getTechLevel() == TechConstants.T_CLAN_LEVEL_2) {
			return 2;
        } else if (hasFlag(F_MASC)) {
            if (entity.getTechLevel() == TechConstants.T_CLAN_LEVEL_2) {
                return Math.round(entity.getWeight() / 25.0f);
            }
            else {
                return Math.round(entity.getWeight() / 20.0f);
            }
        } else if (hasFlag(F_TARGCOMP)) {
           // based on tonnage of direct_fire weaponry
            double fTons = 0.0;
            for (Enumeration e = entity.getWeapons(); e.hasMoreElements(); ) {
                Mounted m = (Mounted)e.nextElement();
                WeaponType wt = (WeaponType)m.getType();
                if (wt.hasFlag(WeaponType.F_DIRECT_FIRE)) {
                    fTons += wt.getTonnage(entity);
                }
            }
            if (entity.getTechLevel() == TechConstants.T_CLAN_LEVEL_2) {
                return (int)Math.ceil(fTons / 5.0f);
            }
            else {
                return (int)Math.ceil(fTons / 4.0f);
            }
        }
        // right, well I'll just guess then
        return 1;
    }
    
    public double getBV(Entity entity) {
        if (bv != BV_VARIABLE) {
            return bv;
        }
        // check for known formulas
        if (hasFlag(F_HATCHET)) {
            return ((float)Math.ceil(entity.getWeight() / 5.0) * 1.5f);
        } else if (hasFlag(F_TARGCOMP)) {
            // 20% of direct_fire weaponry BV (half for rear-facing)
            double fFrontBV = 0.0, fRearBV = 0.0;
            for (Enumeration e = entity.getWeapons(); e.hasMoreElements(); ) {
                Mounted m = (Mounted)e.nextElement();
                WeaponType wt = (WeaponType)m.getType();
                if (wt.hasFlag(WeaponType.F_DIRECT_FIRE)) {
                    if (m.isRearMounted()) {
                        fRearBV += wt.getBV(entity);
                    } else {
                        fFrontBV += wt.getBV(entity);
                    }
                }
            }
            if (fFrontBV > fRearBV) {
                return (float)(fFrontBV * 0.2 + fRearBV * 0.1);
            } else {
                return (float)(fRearBV * 0.2 + fFrontBV * 0.1);
            }
        }
        // maybe it's 0
        return 0;
    }
    
    
    /**
     * Add all the types of misc eq we can create to the list
     */
    public static void initializeTypes() {
        // all tech level 1 stuff
        EquipmentType.addType(createHeatSink());
        EquipmentType.addType(createJumpJet());
        EquipmentType.addType(createTreeClub());
        EquipmentType.addType(createGirderClub());
        EquipmentType.addType(createLimbClub());
        EquipmentType.addType(createHatchet());
        
        // Start of Level2 stuff
        EquipmentType.addType(createDoubleHeatSink());
        EquipmentType.addType(createISDoubleHeatSink());
        EquipmentType.addType(createCLDoubleHeatSink());
        EquipmentType.addType(createISCASE());
        EquipmentType.addType(createCLCASE());
        EquipmentType.addType(createISMASC());
        EquipmentType.addType(createCLMASC());
        EquipmentType.addType(createTSM());
        EquipmentType.addType(createC3S());
        EquipmentType.addType(createC3M());
        EquipmentType.addType(createC3I());
        EquipmentType.addType(createISArtemis());
        EquipmentType.addType(createCLArtemis());
        EquipmentType.addType(createGECM());
        EquipmentType.addType(createCLECM());
        EquipmentType.addType(createISTargComp());
        EquipmentType.addType(createCLTargComp());
        EquipmentType.addType(createMekStealth());

        // Start BattleArmor equipment
        EquipmentType.addType( createBABoardingClaw() );
        EquipmentType.addType( createBAAssaultClaws() );
        EquipmentType.addType( createBAFireProtection() );
        EquipmentType.addType( createStealth() );
        EquipmentType.addType( createAdvancedStealth() );
        EquipmentType.addType( createExpertStealth() );
        EquipmentType.addType( createMine() );
        EquipmentType.addType( createMinesweeper() );
        EquipmentType.addType( createBAMagneticClamp() );
        EquipmentType.addType( createSingleHexECM() );
        EquipmentType.addType( createMimeticCamo() );
        EquipmentType.addType( createParafoil() );
    }
    
    public static MiscType createHeatSink() {
        MiscType misc = new MiscType();
        
        misc.name = "Heat Sink";
        misc.internalName = misc.name;
        misc.mepName = misc.name;
        misc.mtfName = misc.name;
        misc.tonnage = 1.0f;
        misc.criticals = 1;
        misc.flags |= F_HEAT_SINK;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createJumpJet() {
        MiscType misc = new MiscType();
        
        misc.name = "Jump Jet";
        misc.internalName = misc.name;
        misc.mepName = misc.name;
        misc.mtfName = misc.name;
        misc.tonnage = TONNAGE_VARIABLE;
        misc.criticals = 1;
        misc.flags |= F_JUMP_JET;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createTreeClub() {
        MiscType misc = new MiscType();
        
        misc.name = "Tree Club";
        misc.internalName = misc.name;
        misc.mepName = "N/A";
        misc.mtfName = misc.mepName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.flags |= F_TREE_CLUB | F_CLUB;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createGirderClub() {
        MiscType misc = new MiscType();
        
        misc.name = "Girder Club";
        misc.internalName = misc.name;
        misc.mepName = "N/A";
        misc.mtfName = misc.mepName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.flags |= F_CLUB;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createLimbClub() {
        MiscType misc = new MiscType();
        
        misc.name = "Limb Club";
        misc.internalName = misc.name;
        misc.mepName = "N/A";
        misc.mtfName = misc.mepName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.flags |= F_CLUB;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createHatchet() {
        MiscType misc = new MiscType();
        
        misc.name = "Hatchet";
        misc.internalName = misc.name;
        misc.mepName = misc.name;
        misc.mtfName = misc.name;
        misc.tonnage = TONNAGE_VARIABLE;
        misc.criticals = CRITICALS_VARIABLE;
        misc.flags |= F_HATCHET;
        misc.bv = BV_VARIABLE;
        
        return misc;
    }
    
    // Start of Level2 stuff
    
    // REMOVE ME WHEN HMPREAD IS UPDATED!
    public static MiscType createDoubleHeatSink() {
        MiscType misc = new MiscType();
        
        misc.name = "Double Heat Sink";
        misc.internalName = "REMOVE MEEE!!";
        misc.mepName = "REMOVE ME!";
        misc.mtfName = "Double Heat Sink";
        misc.tonnage = 1.0f;
        misc.criticals = CRITICALS_VARIABLE;
        misc.flags |= F_DOUBLE_HEAT_SINK;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createISDoubleHeatSink() {
        MiscType misc = new MiscType();
        
        misc.name = "Double Heat Sink";
        misc.internalName = "ISDoubleHeatSink";
        misc.mepName = "IS Double Heat Sink";
        misc.mtfName = "ISDouble Heat Sink";
        misc.tonnage = 1.0f;
        misc.criticals = 3;
        misc.flags |= F_DOUBLE_HEAT_SINK;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createCLDoubleHeatSink() {
        MiscType misc = new MiscType();
        
        misc.name = "Double Heat Sink";
        misc.internalName = "CLDoubleHeatSink";
        misc.mepName = "Clan Double Heat Sink";
        misc.mtfName = "CLDouble Heat Sink";
        misc.tonnage = 1.0f;
        misc.criticals = 2;
        misc.flags |= F_DOUBLE_HEAT_SINK;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createISCASE() {
        MiscType misc = new MiscType();
        
        misc.name = "CASE";
        misc.internalName = "ISCASE";
        misc.mepName ="IS CASE";
        misc.mtfName = "ISCASE";
        misc.tonnage = 0.5f;
        misc.criticals = 1;
        misc.hittable = false;
        misc.flags |= F_CASE;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createCLCASE() {
        MiscType misc = new MiscType();
        
        misc.name = "CASE";
        misc.internalName = "CLCASE";
        misc.mepName = "Clan CASE";
        misc.mtfName = "CLCASE";
        misc.tonnage = 0.0f;
        misc.criticals = 0;
        misc.hittable = false;
        misc.flags |= F_CASE;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createISMASC() {
        MiscType misc = new MiscType();
        
        misc.name = "MASC";
        misc.internalName = "ISMASC";
        misc.mepName = "IS MASC";
        misc.mtfName = misc.internalName;
        misc.tonnage = TONNAGE_VARIABLE;
        misc.criticals = CRITICALS_VARIABLE;
        misc.hittable = false;
        misc.spreadable = true;
        misc.flags |= F_MASC;
        misc.bv = 0;
        
        String[] saModes = { "Off", "On" };
        misc.setModes(saModes);
        
        return misc;
    }
    
    public static MiscType createCLMASC() {
        MiscType misc = new MiscType();
        
        misc.name = "MASC";
        misc.internalName = "CLMASC";
        misc.mepName = "Clan MASC";
        misc.mtfName = misc.internalName;
        misc.tonnage = TONNAGE_VARIABLE;
        misc.criticals = CRITICALS_VARIABLE;
        misc.hittable = false;
        misc.spreadable = true;
        misc.flags |= F_MASC;
        misc.bv = 0;
        
        String[] saModes = { "Off", "On" };
        misc.setModes(saModes);
        
        return misc;
    }
    
    public static MiscType createTSM() {
        MiscType misc = new MiscType();
        
        misc.name = "TSM";
        misc.internalName = misc.name;
        misc.mepName = "IS TSM";
        misc.mtfName = "Triple Strength Myomer";
        misc.tonnage = 0;
        misc.criticals = 6;
        misc.hittable = false;
        misc.spreadable = true;
        misc.flags |= F_TSM;
        misc.bv = 0;
        
        return misc;
    }

    public static MiscType createC3S() {
        MiscType misc = new MiscType();
        
        misc.name = "C3 Slave";
        misc.internalName = "ISC3SlaveUnit";
        misc.mepName = "IS C3 Slave";
        misc.mtfName = "ISC3SlaveUnit";
        misc.tonnage = 1;
        misc.criticals = 1;
        misc.hittable = true;
        misc.spreadable = false;
        misc.flags |= F_C3S;
        misc.bv = 0;
        
        return misc;
    }

    public static MiscType createC3M() {
        MiscType misc = new MiscType();
        
        misc.name = "C3 Master";
        misc.internalName = misc.name;
        misc.mepName = "IS C3 Computer";
        misc.mtfName = "ISC3MasterComputer";
        misc.tonnage = 5;
        misc.criticals = 5;
        misc.hittable = true;
        misc.spreadable = false;
        misc.flags |= F_C3M;
        misc.bv = 0;
        
        return misc;
    }

    public static MiscType createC3I() {
        MiscType misc = new MiscType();
        
        misc.name = "C3i Computer";
        misc.internalName = misc.name;
        misc.mepName = misc.name;
        misc.mtfName = "ISImprovedC3CPU";
        misc.tonnage = 2.5f;
        misc.criticals = 2;
        misc.hittable = true;
        misc.spreadable = false;
        misc.flags |= F_C3I;
        misc.bv = 0;
        
        return misc;
    }
    
    public static MiscType createISArtemis() {
        MiscType misc = new MiscType();
        misc.name = "Artemis IV FCS";
        misc.mtfName = "ISArtemisIV";
        misc.mepName = "IS Artemis IV FCS";
        misc.internalName = misc.mtfName;
        misc.tonnage = 1.0f;
        misc.criticals = 1;
        misc.flags |= F_ARTEMIS;
        return misc;
    }
    
    public static MiscType createCLArtemis() {
        MiscType misc = new MiscType();
        misc.name = "Artemis IV FCS";
        misc.mtfName = "CLArtemisIV";
        misc.mepName = "Clan Artemis IV FCS";
        misc.internalName = misc.mtfName;
        misc.tonnage = 1.0f;
        misc.criticals = 1;
        misc.flags |= F_ARTEMIS;
        return misc;
    }
        
    public static MiscType createGECM() {
        MiscType misc = new MiscType();
        
        misc.name = "Guardian ECM Suite";
        misc.internalName = misc.name;
        misc.mepName = "IS Guardian ECM";
        misc.mtfName = "ISGuardianECM";
        misc.tonnage = 1.5f;
        misc.criticals = 2;
        misc.hittable = true;
        misc.spreadable = false;
        misc.flags |= F_ECM;
        misc.bv = 61;
        
        return misc;
    }

    public static MiscType createCLECM() {
        MiscType misc = new MiscType();
        
        misc.name = "ECM Suite";
        misc.internalName = misc.name;
        misc.mepName = "Clan ECM Suite";
        misc.mtfName = "CLECMSuite";
        misc.tonnage = 1;
        misc.criticals = 1;
        misc.hittable = true;
        misc.spreadable = false;
        misc.flags |= F_ECM;
        misc.bv = 61;
        
        return misc;
    }
    
    /**
     * Targeting comps should NOT be spreadable.  However, I've set them such
     * as a temp measure to overcome the following bug:
     * TC space allocation is calculated based on tonnage of direct-fire weaponry.
     * However, since meks are loaded location-by-location, when the TC is loaded
     * it's very unlikely that all of the weaponry will be attached, resulting in
     * undersized comps.  Any remaining TC crits after the last expected one are
     * being handled as a 2nd TC, causing LocationFullExceptions.
     */
    
    public static MiscType createISTargComp() {
        MiscType misc = new MiscType();
        
        misc.name = "Targeting Computer";
        misc.internalName = "ISTargeting Computer";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = TONNAGE_VARIABLE;
        misc.criticals = CRITICALS_VARIABLE;
        misc.bv = BV_VARIABLE;
        misc.flags |= F_TARGCOMP;
        // see note above
        misc.spreadable = true;
        
        return misc;
    }
    
    public static MiscType createCLTargComp() {
        MiscType misc = new MiscType();
        
        misc.name = "Targeting Computer";
        misc.internalName = "CLTargeting Computer";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = TONNAGE_VARIABLE;
        misc.criticals = CRITICALS_VARIABLE;
        misc.bv = BV_VARIABLE;
        misc.flags |= F_TARGCOMP;
        // see note above
        misc.spreadable = true;
        
        return misc;
    }

    // Start BattleArmor equipment
    public static MiscType createBABoardingClaw() {
        MiscType misc = new MiscType();
        
        misc.name = "Boarding Claw";
        misc.internalName = BattleArmor.BOARDING_CLAW;
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createBAAssaultClaws() {
        MiscType misc = new MiscType();
        
        misc.name = "Assault Claws";
        misc.internalName = BattleArmor.ASSAULT_CLAW;
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createBAFireProtection() {
        MiscType misc = new MiscType();
        
        misc.name = "Fire Protection";
        misc.internalName = BattleArmor.FIRE_PROTECTION;
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createStealth() {
        MiscType misc = new MiscType();
        
        misc.name = "Stealth";
        misc.internalName = "Stealth";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createAdvancedStealth() {
        MiscType misc = new MiscType();
        
        misc.name = "Advanced Stealth";
        misc.internalName = "Advanced Stealth";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createExpertStealth() {
        MiscType misc = new MiscType();
        
        misc.name = "Expert Stealth";
        misc.internalName = "Expert Stealth";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createMine() {
        MiscType misc = new MiscType();
        
        misc.name = "Mine";
        misc.internalName = "Mine";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = true;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createMinesweeper() {
        MiscType misc = new MiscType();
        
        misc.name = "Minesweeper";
        misc.internalName = "Minesweeper";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createBAMagneticClamp() {
        MiscType misc = new MiscType();
        
        misc.name = "Magnetic Clamp";
        misc.internalName = BattleArmor.MAGNETIC_CLAMP;
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createSingleHexECM() {
        MiscType misc = new MiscType();
        
        misc.name = "Single-Hex ECM";
        misc.internalName = "Single-Hex ECM";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_ECM;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createMimeticCamo() {
        MiscType misc = new MiscType();
        
        misc.name = "Mimetic Camoflage";
        misc.internalName = "Mimetic Camo";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }
    public static MiscType createParafoil() {
        MiscType misc = new MiscType();
        
        misc.name = "Parafoil";
        misc.internalName = "Parafoil";
        misc.mepName = misc.internalName;
        misc.mtfName = misc.internalName;
        misc.tonnage = 0;
        misc.criticals = 0;
        misc.hittable = false;
        misc.spreadable = false;
        misc.flags |= F_OTHER;
        misc.bv = 0;
        
        return misc;
    }

    public static MiscType createMekStealth() {
        MiscType misc = new MiscType();
        
        misc.name = "Stealth Armor";
        misc.internalName = Mech.STEALTH;
        misc.mepName = misc.internalName;
        misc.mtfName = "Stealth Armor";
        misc.tonnage = 0;       //???
        misc.criticals = 12;
        misc.hittable = false;
        misc.spreadable = true;
        misc.flags |= F_OTHER;
        String[] saModes = { "Off", "On" };
        misc.setModes(saModes);
        misc.setInstantModeSwitch(false);
        misc.bv = 0;            //???
        
        return misc;
    }

}
